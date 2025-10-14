from __future__ import annotations
import pandas as pd
import xlrd  # use legacy xlrd to read .xls
from pathlib import Path
import shutil
from datetime import timedelta
from typing import Dict, List
import argparse

BASE = Path(__file__).parent
SRC_XLS = BASE / 'contest_record.xls'
SRC_CSV = BASE / 'contest_record.csv'
DST = BASE / 'in.xls'
BACKUP = BASE / 'in.backup.xls'

# 读取 .xls 的兼容性设置
READ_ENGINE = 'xlrd'  # 读取 .xls 最稳妥
WRITE_ENGINE = 'openpyxl'  # 目标文件若被另存为 .xlsx 时使用；直接写 .xls 用 xlwt

NEEDED_COLS = [
    'cid','uid','pid','cpid','submit_id','display_id','username','realname','status',
    'submit_time','time','score','use_time'
]

# 罚时规则（单位：秒）：每题罚时 = 首次 AC 的 time(秒) + 首 AC 前错误提交次数 × 20 分钟（1200 秒）
# 未 AC 的题，不计入罚时


def load_records_xls(path: Path) -> pd.DataFrame:
    wb = xlrd.open_workbook(path)
    sheet = wb.sheet_by_index(0)
    # 假设第一行是表头；若不是，尝试在前 5 行内寻找包含关键列名的一行
    header_row = 0
    key_tokens = {'uid','用户id','用户ID','用户'}
    max_rows_check = min(5, sheet.nrows)
    for r in range(max_rows_check):
        row_vals = [str(sheet.cell_value(r, c)).strip() for c in range(sheet.ncols)]
        if any(tok in row_vals for tok in key_tokens):
            header_row = r
            break
    headers = [str(sheet.cell_value(header_row, c)).strip() for c in range(sheet.ncols)]
    records = []
    for r in range(header_row + 1, sheet.nrows):
        row = {}
        for c, h in enumerate(headers):
            row[str(h)] = sheet.cell_value(r, c)
        records.append(row)
    df = pd.DataFrame(records)
    return df


def load_records_csv(path: Path) -> pd.DataFrame:
    return pd.read_csv(path)


def load_records() -> pd.DataFrame:
    # 优先使用 CSV，其次使用 XLS
    if SRC_CSV.exists():
        df = load_records_csv(SRC_CSV)
    elif SRC_XLS.exists():
        df = load_records_xls(SRC_XLS)
    else:
        raise FileNotFoundError('未找到 contest_record.csv 或 contest_record.xls')
    # 标准化列名
    df.columns = [str(c).strip() for c in df.columns]
    # 兼容可能的中文列名
    alias = {
        '用户id':'uid', '用户ID':'uid', '用户':'uid',
        '题目id':'pid', '题目ID':'pid',
        '比赛id':'cid', '比赛ID':'cid',
        '比赛题目id':'cpid', '比赛中题目id':'cpid',
        '提交id':'submit_id', '提交ID':'submit_id',
        '用户名':'username', '真实姓名':'realname', '真实名称':'realname',
        '提交时间':'submit_time', '运行时间':'use_time',
        '得分':'score', '状态':'status', '时间':'time', '展示id':'display_id'
    }
    df.rename(columns={k:v for k,v in alias.items() if k in df.columns}, inplace=True)
    # 仅保留所需列，缺失的补齐
    for c in NEEDED_COLS:
        if c not in df.columns:
            df[c] = pd.NA
    df = df[NEEDED_COLS].copy()

    # 类型处理
    if 'submit_time' in df.columns:
        try:
            df['submit_time'] = pd.to_datetime(df['submit_time'], errors='coerce')
        except Exception:
            pass
    for int_col in ['pid','cpid','submit_id','status','time','score','use_time']:
        if int_col in df.columns:
            df[int_col] = pd.to_numeric(df[int_col], errors='coerce').astype('Int64')
    for str_col in ['cid','uid','display_id','username','realname']:
        if str_col in df.columns:
            df[str_col] = df[str_col].astype(str)
    return df


def _to_int(value) -> int | pd._libs.missing.NAType:
    if pd.isna(value):
        return pd.NA
    try:
        return int(float(value))
    except Exception:
        return pd.NA


def compute_per_user_problem(records: pd.DataFrame) -> pd.DataFrame:
    # 按用户+题目分组，找到首次 AC 的时间点与其之前错误次数（time 为从比赛开始的秒数）
    def summarize(group: pd.DataFrame):
        g = group.sort_values(by=['time','submit_time'], na_position='last')
        # 首 AC 记录
        ac_rows = g[g['status'] == 1]
        if len(ac_rows) == 0:
            return pd.Series({
                'ac': 0,
                'first_ac_time_seconds': pd.NA,
                'wrong_before_ac': 0,
                'penalty_seconds': 0,
                'submits': len(g),
            })
        first_ac = ac_rows.iloc[0]
        # 统计在这个 first_ac 之前的错误提交数（status == -1）
        # 使用排序后的行位置来切片，避免依赖索引比较歧义
        first_idx = first_ac.name
        try:
            pos = g.index.get_loc(first_idx)
            if isinstance(pos, slice):
                pos = pos.start  # 理论上不应发生，兜底
        except Exception:
            pos = list(g.index).index(first_idx)
        before = g.iloc[:pos]  # first_ac 之前，不含 first_ac 本身
        wrong_before = int((before['status'] == -1).sum())
        time_sec = _to_int(first_ac['time'])
        penalty = (time_sec if pd.notna(time_sec) else 0) + wrong_before * 1200
        return pd.Series({
            'ac': 1,
            'first_ac_time_seconds': time_sec,
            'wrong_before_ac': wrong_before,
            'penalty_seconds': int(penalty),
            'submits': len(g),
        })

    key_cols = ['cid','uid','pid','cpid','display_id','username','realname']
    for c in key_cols:
        if c not in records.columns:
            records[c] = pd.NA
    grouped = records.groupby(['cid','uid','pid'], dropna=False)
    summary = grouped.apply(summarize, include_groups=False).reset_index()
    # 回填一些辅助信息（取该组中的代表值）
    rep = records.groupby(['cid','uid','pid']).agg({
        'cpid':'first','display_id':'first','username':'first','realname':'first'
    }).reset_index()
    out = rep.merge(summary, on=['cid','uid','pid'], how='left')
    # 排序：比赛 -> 用户 -> 是否AC(降序) -> 罚时升序 -> 题目
    out['ac'] = out['ac'].fillna(0).astype(int)
    out['penalty_seconds'] = out['penalty_seconds'].fillna(0).astype(int)
    out = out.sort_values(['cid','uid','ac','penalty_seconds','pid'], ascending=[True, True, False, True, True])
    return out


def compute_user_total_penalty(per_problem: pd.DataFrame) -> pd.DataFrame:
    # 仅统计 AC 的题目罚时（单位：秒）
    tmp = per_problem[per_problem['ac'] == 1].copy()
    totals = tmp.groupby(['cid','uid'], dropna=False)['penalty_seconds'].sum().reset_index()
    totals.rename(columns={'penalty_seconds':'total_penalty_seconds'}, inplace=True)
    # 计算 AC 题数
    solved = tmp.groupby(['cid','uid'], dropna=False)['ac'].count().reset_index().rename(columns={'ac':'solved_cnt'})
    out = totals.merge(solved, on=['cid','uid'], how='outer').fillna({'total_penalty_seconds':0,'solved_cnt':0}).copy()
    out['total_penalty_seconds'] = out['total_penalty_seconds'].astype(int)
    out['solved_cnt'] = out['solved_cnt'].astype(int)
    # 追加用户名展示
    names = per_problem.groupby(['cid','uid'], dropna=False)['username'].first().reset_index()
    out = out.merge(names, on=['cid','uid'], how='left')
    # 排序：比赛 -> 解题数降序 -> 总罚时升序 -> uid
    out = out.sort_values(['cid','solved_cnt','total_penalty_seconds','uid'], ascending=[True, False, True, True])
    return out


def _read_all_sheets_xls(path: Path) -> Dict[str, pd.DataFrame]:
    wb = xlrd.open_workbook(path)
    out: Dict[str, pd.DataFrame] = {}
    for sheet_name in wb.sheet_names():
        sh = wb.sheet_by_name(sheet_name)
        if sh.nrows == 0:
            out[sheet_name] = pd.DataFrame()
            continue
        headers = [str(sh.cell_value(0, c)).strip() for c in range(sh.ncols)]
        data = []
        for r in range(1, sh.nrows):
            row = {}
            for c, h in enumerate(headers):
                row[h] = sh.cell_value(r, c)
            data.append(row)
        out[sheet_name] = pd.DataFrame(data)
    return out


def _sec_to_hhmmss(total_seconds: int) -> str:
    if pd.isna(total_seconds):
        return ''
    total_seconds = int(total_seconds)
    h = total_seconds // 3600
    m = (total_seconds % 3600) // 60
    s = total_seconds % 60
    return f"{h:02d}:{m:02d}:{s:02d}"


def _prepare_bangdan_rows(per_problem: pd.DataFrame, headers: List[str]) -> List[List[object]]:
    # 提取“题目列”字母，如 A, B, C, ... 根据现有表头来决定。
    letters: List[str] = []
    for h in headers:
        if len(h) == 1 and 'A' <= h <= 'Z':
            letters.append(h)

    # 每个用户的 per-problem 明细（按 display_id 汇总）
    # per_problem 包含：cid, uid, pid, cpid, display_id, username, realname, ac, wrong_before_ac,
    # first_ac_time_seconds(秒), penalty_seconds(秒) 等
    detail = per_problem.copy()
    # 确保字段存在
    if 'first_ac_time_seconds' not in detail.columns:
        detail['first_ac_time_seconds'] = pd.NA
    if 'penalty_seconds' not in detail.columns:
        detail['penalty_seconds'] = pd.NA

    # 以 uid -> username
    name_map = detail.groupby('uid', dropna=False)['username'].first().to_dict()
    realname_map = detail.groupby('uid', dropna=False)['realname'].first().to_dict()

    # 生成 per_user 总罚时（以秒计）与解题数：first_ac_time_seconds + wrong_before_ac*1200
    def per_user_agg(df: pd.DataFrame):
        df_ac = df[df['ac'] == 1]
        solved = int(len(df_ac))
        total_sec = 0
        for _, row in df_ac.iterrows():
            wrong = int(row.get('wrong_before_ac', 0) or 0)
            sec = row.get('first_ac_time_seconds', pd.NA)
            if pd.isna(sec):
                # 兜底：从 penalty_seconds 中扣除错误罚时得到首 AC 秒数
                psec = row.get('penalty_seconds', pd.NA)
                if pd.notna(psec):
                    sec = max(0, int(psec) - wrong * 1200)
                else:
                    sec = 0
            total_sec += int(sec) + wrong * 1200
        return pd.Series({'solved_cnt': solved, 'total_penalty_seconds': int(total_sec)})

    per_user = detail.groupby('uid', dropna=False).apply(per_user_agg, include_groups=False).reset_index()
    # 排序：解题数降序，总罚时升序
    per_user = per_user.sort_values(['solved_cnt','total_penalty_seconds'], ascending=[False, True])

    # 为每个用户构造题目列字符串，如 '00:02:59(-1)'
    # 制作 uid->display_id->(sec, wrong)
    def select_first(df: pd.DataFrame):
        # df 是单个 uid+display_id 的集合，但我们有 per_problem 一行就代表一题，直接取即可
        row = df.iloc[0]
        wrong = int(row.get('wrong_before_ac', 0) or 0)
        sec = row.get('first_ac_time_seconds', pd.NA)
        if pd.isna(sec):
            psec = row.get('penalty_seconds', pd.NA)
            if pd.notna(psec):
                sec = max(0, int(psec) - wrong * 1200)
        return pd.Series({'sec': sec, 'wrong': wrong, 'ac': int(row.get('ac', 0) or 0)})

    by_uid_disp = detail.groupby(['uid','display_id'], dropna=False).apply(select_first, include_groups=False).reset_index()
    # 构造字典便于快速查
    lookup = {}
    for _, r in by_uid_disp.iterrows():
        uid = str(r['uid'])
        disp = str(r['display_id']).strip()
        if uid not in lookup:
            lookup[uid] = {}
        lookup[uid][disp] = {'sec': r['sec'], 'wrong': int(r.get('wrong', 0) or 0), 'ac': int(r.get('ac', 0) or 0)}

    # 拼装每行值，按 headers 的顺序
    rows_out: List[List[object]] = []
    for _, urow in per_user.iterrows():
        uid = str(urow['uid']) if 'uid' in urow else str(urow['uid'])
        username = name_map.get(urow['uid'], '')
        realname = realname_map.get(urow['uid'], '')
        solved = int(urow['solved_cnt'])
        total_penalty = int(urow['total_penalty_seconds'])
        row_vals: List[object] = []
        # 预构造题目列字典
        disp_map = lookup.get(str(urow['uid']), {})

        for h in headers:
            val: object = ''
            if h == '排名':
                val = ''
            elif h == '团队':
                val = '否'
            elif h == '昵称':
                base_name = str(username) if username and username != 'nan' else ''
                # 仅保留用户名；无用户名时退回 uid
                val = base_name if base_name else uid
            elif h == '真实名称':
                rn = str(realname) if realname and realname != 'nan' else ''
                val = rn
            elif h in ('学历','邮箱','手机号码','学校','毕业年份','学号','专业班级'):
                val = ''
            elif h == '通过题数':
                val = solved
            elif h == '罚时':
                val = _sec_to_hhmmss(total_penalty)
            elif len(h) == 1 and 'A' <= h <= 'Z':
                d = disp_map.get(h)
                if d and int(d.get('ac', 0)) == 1:
                    sec = d.get('sec', pd.NA)
                    t = _sec_to_hhmmss(sec if pd.notna(sec) else 0)
                    wrong = int(d.get('wrong', 0) or 0)
                    val = f"{t}(-{wrong})" if wrong > 0 else t
                else:
                    val = ''
            elif h.endswith('-相似度'):
                val = ''
            else:
                val = ''
            row_vals.append(val)
        rows_out.append(row_vals)
    return rows_out


def append_to_bangdan(per_problem: pd.DataFrame):
    import xlrd
    from xlutils.copy import copy as xl_copy
    # 备份 in.xls
    if DST.exists():
        shutil.copyfile(DST, BACKUP)
    # 读 in.xls
    rb = xlrd.open_workbook(DST)
    sh = rb.sheet_by_name('榜单')
    headers = [str(sh.cell_value(0, c)).strip() for c in range(sh.ncols)]
    start_row = sh.nrows  # 从最后一行后追加
    # 准备要追加的多行
    rows = _prepare_bangdan_rows(per_problem, headers)
    # 写回
    wb = xl_copy(rb)
    ws = wb.get_sheet(rb.sheet_names().index('榜单'))
    r = start_row
    for row in rows:
        for c, v in enumerate(row):
            # xlwt 只支持写原始值，字符串/数字均可
            ws.write(r, c, v)
        r += 1
    wb.save(str(DST))
    print(f'已在 原始 in.xls 的“榜单”表末尾追加 {len(rows)} 行，备份文件: {BACKUP}')


def refresh_bangdan_and_append(per_problem: pd.DataFrame):
    """
    清理“榜单”中由脚本追加产生的空“排名”行，再按当前统计结果重新追加。
    说明：原始榜单前几行通常“排名”为数字，本脚本追加的行“排名”留空 -> 以此作为清理条件。
    """
    import xlrd
    from xlutils.copy import copy as xl_copy
    # 备份 in.xls
    if DST.exists():
        shutil.copyfile(DST, BACKUP)
    rb = xlrd.open_workbook(DST)
    sh = rb.sheet_by_name('榜单')
    headers = [str(sh.cell_value(0, c)).strip() for c in range(sh.ncols)]
    # 先准备要追加的新行
    new_rows = _prepare_bangdan_rows(per_problem, headers)
    # 清理逻辑：将“排名”空白的行清空（写空串），避免重复
    wb = xl_copy(rb)
    ws = wb.get_sheet(rb.sheet_names().index('榜单'))
    for r in range(1, sh.nrows):
        rank_val = sh.cell_value(r, 0)
        if rank_val in ('', None):
            for c in range(sh.ncols):
                ws.write(r, c, '')
    # 找到新的尾行（由于清空不改变行数，这里仍用原 nrows 作为追加起点）
    start_row = sh.nrows
    r = start_row
    for row in new_rows:
        for c, v in enumerate(row):
            ws.write(r, c, v)
        r += 1
    wb.save(str(DST))
    print(f'已清理空排名行并在“榜单”末尾追加 {len(new_rows)} 行，备份文件: {BACKUP}')

def main():
    parser = argparse.ArgumentParser(description='Import contest records into in.xls (榜单).')
    parser.add_argument('--refresh-bangdan', action='store_true', help='清理空排名行后再追加，避免重复')
    args = parser.parse_args()

    # 加载记录（优先 CSV，其次 XLS），内部会自行检查存在性
    rec = load_records()
    per_problem = compute_per_user_problem(rec)
    # 直接追加或先清理再追加
    if args.refresh_bangdan:
        refresh_bangdan_and_append(per_problem)
    else:
        append_to_bangdan(per_problem)

if __name__ == '__main__':
    main()
