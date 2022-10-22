<!--评论模块-->
<template>
  <div>
    <div
      v-if="cid"
      class="comment-top container"
      :style="cid ? 'max-width: 100% !important;' : ''"
    >
      <el-alert
        type="warning"
        effect="dark"
        center
        :closable="false"
        style="background-color: rgb(14, 176, 201)!important;"
      >
        <template slot="title">
          <span class="title">{{
            $i18n.t('m.Announcement_of_contest_Q_and_A_area')
          }}</span></template
        >
        <template slot>
          <p>
            1. {{ $i18n.t('m.Announcement_of_contest_Q_and_A_area_tips1') }}
          </p>
          <p>
            2. {{ $i18n.t('m.Announcement_of_contest_Q_and_A_area_tips2') }}
          </p>
          <p>
            3. {{ $i18n.t('m.Announcement_of_contest_Q_and_A_area_tips3') }}
          </p>
        </template>
      </el-alert>
    </div>
    <div class="container" :style="cid ? 'max-width: 100% !important;' : ''">
      <div class="own-input">
        <el-input
          v-model="ownInputComment"
          type="textarea"
          :rows="8"
          id="own-textarea"
          :placeholder="$t('m.Come_and_write_down_your_comments') + '~😘'"
        >
        </el-input>
        <div class="input-bottom">
          <span title="emoji">
            <el-popover
              placement="top-start"
              width="300"
              trigger="click"
              v-model="facelistVisiable"
            >
              <div class="emotionList face-box">
                <a
                  href="javascript:void(0);"
                  @click="getEmo(index, true)"
                  v-for="(item, index) in faceList"
                  :key="index"
                  class="emotionItem"
                  >{{ item }}</a
                >
              </div>
              <i class="fa fa-smile-o emotionSelect" slot="reference"></i>
            </el-popover>
          </span>
          <span
            class="markdown-key"
            :title="$t('m.Inline_Code')"
            @click="addContentTips(0, false)"
            ><svg
              xmlns="http://www.w3.org/2000/svg"
              viewBox="0 0 24 24"
              width="1em"
              height="1em"
              fill="currentColor"
              class="css-1rhb60f-Svg ea8ky5j0"
            >
              <path
                fill-rule="evenodd"
                d="M5.5 5h1.866a.5.5 0 01.486.617l-.96 4a.5.5 0 01-.486.383H5.5a.5.5 0 01-.5-.5v-4a.5.5 0 01.5-.5zm8 0h1.866a.5.5 0 01.486.617l-.96 4a.5.5 0 01-.486.383H13.5a.5.5 0 01-.5-.5v-4a.5.5 0 01.5-.5z"
                clip-rule="evenodd"
              ></path>
            </svg>
          </span>
          <span
            class="markdown-key"
            :title="$t('m.Code_Block')"
            @click="addContentTips(1, false)"
          >
            <svg
              xmlns="http://www.w3.org/2000/svg"
              viewBox="0 0 24 24"
              width="1em"
              height="1em"
              fill="currentColor"
              class="css-1rhb60f-Svg ea8ky5j0"
            >
              <path
                fill-rule="evenodd"
                d="M3.64 10.98c.147 0 .312-.022.495-.066.183-.044.348-.12.495-.23.147-.11.271-.269.374-.474.103-.205.154-.462.154-.77V5.084c0-.499.077-.928.231-1.287.154-.36.363-.66.627-.902s.565-.422.902-.539c.337-.117.69-.176 1.056-.176H9.58v1.848H8.37c-.425 0-.693.14-.803.418-.11.279-.165.58-.165.902v4.136c0 .425-.08.788-.242 1.09-.161.3-.352.545-.572.736-.22.19-.455.337-.704.44-.25.103-.462.161-.638.176v.044c.176.015.389.062.638.143.25.08.484.216.704.407.22.19.41.447.572.77.161.323.242.74.242 1.254v4.004c0 .323.055.623.165.902.11.279.378.418.803.418h1.21v1.848H7.974c-.367 0-.719-.059-1.056-.176a2.573 2.573 0 01-.902-.539 2.586 2.586 0 01-.627-.902c-.154-.36-.231-.788-.231-1.287V14.61c0-.352-.051-.638-.154-.858a1.494 1.494 0 00-.374-.517 1.18 1.18 0 00-.495-.253 2.143 2.143 0 00-.495-.066V10.98zm16.72 2.04c-.161 0-.33.022-.506.066a1.184 1.184 0 00-.484.253 1.494 1.494 0 00-.374.517c-.103.22-.154.506-.154.858v4.202c0 .499-.077.928-.231 1.287-.154.36-.363.66-.627.902a2.573 2.573 0 01-.902.54c-.337.116-.69.175-1.056.175H14.42v-1.848h1.21c.425 0 .693-.14.803-.418.11-.279.165-.58.165-.902v-4.004c0-.513.08-.931.242-1.254.161-.323.352-.58.572-.77.22-.19.455-.326.704-.407.25-.08.462-.128.638-.143v-.044a2.225 2.225 0 01-.638-.176 2.567 2.567 0 01-.704-.44c-.22-.19-.41-.436-.572-.737-.161-.3-.242-.664-.242-1.089V5.452c0-.323-.055-.623-.165-.902-.11-.279-.378-.418-.803-.418h-1.21V2.284h1.606c.367 0 .719.059 1.056.176.337.117.638.297.902.539.264.242.473.543.627.902.154.36.231.788.231 1.287v4.356c0 .308.051.565.154.77.103.205.227.363.374.473.147.11.308.187.484.231.176.044.345.066.506.066v1.936z"
                clip-rule="evenodd"
              ></path>
            </svg>
          </span>
          <span
            class="markdown-key"
            :title="$t('m.Link')"
            @click="addContentTips(2, false)"
            ><svg
              xmlns="http://www.w3.org/2000/svg"
              viewBox="0 0 24 24"
              width="1em"
              height="1em"
              fill="currentColor"
              class="css-1rhb60f-Svg ea8ky5j0"
            >
              <path
                fill-rule="evenodd"
                d="M13 7a1 1 0 011-1h2a6 6 0 010 12h-2a1 1 0 110-2h2a4 4 0 000-8h-2a1 1 0 01-1-1zm-3 10a1 1 0 01-1 1H8A6 6 0 018 6h1a1 1 0 010 2H8a4 4 0 100 8h1a1 1 0 011 1zm-1-6h6a1 1 0 110 2H9a1 1 0 110-2z"
                clip-rule="evenodd"
              ></path></svg
          ></span>
          <span
            class="markdown-key"
            :title="$t('m.Unordered_list')"
            @click="addContentTips(3, false)"
            ><svg
              xmlns="http://www.w3.org/2000/svg"
              viewBox="0 0 24 24"
              width="1em"
              height="1em"
              fill="currentColor"
              class="css-1rhb60f-Svg ea8ky5j0"
            >
              <path
                fill-rule="evenodd"
                d="M8 7a1 1 0 110-2h13a1 1 0 110 2H8zm0 6a1 1 0 110-2h13a1 1 0 110 2H8zm0 6a1 1 0 110-2h13a1 1 0 110 2H8zM2.952 6.85a1.201 1.201 0 111.698-1.7 1.201 1.201 0 01-1.698 1.7zm0 6a1.201 1.201 0 111.698-1.7 1.201 1.201 0 01-1.698 1.7zm0 6a1.201 1.201 0 111.698-1.7 1.201 1.201 0 01-1.698 1.7z"
                clip-rule="evenodd"
              ></path></svg
          ></span>
          <span
            class="markdown-key"
            :title="$t('m.Ordered_List')"
            @click="addContentTips(4, false)"
            ><svg
              xmlns="http://www.w3.org/2000/svg"
              viewBox="0 0 24 24"
              width="1em"
              height="1em"
              fill="currentColor"
              class="css-1rhb60f-Svg ea8ky5j0"
            >
              <path
                fill-rule="evenodd"
                d="M7 6a1 1 0 011-1h13a1 1 0 110 2H8a1 1 0 01-1-1zm1 13a1 1 0 110-2h13a1 1 0 010 2H8zm0-6a1 1 0 110-2h13a1 1 0 010 2H8zM2.756 4.98l.236-1.18h2.22l-.92 4.6h-1.38l.684-3.42h-.84zm2.823 8.3l-.235 1.192H1.756l.174-.87.073-.117 1.911-1.498c.219-.178.368-.324.445-.434a.49.49 0 00.099-.287.208.208 0 00-.083-.18c-.067-.052-.176-.082-.335-.082a.897.897 0 00-.398.094 1.02 1.02 0 00-.342.272l-.12.144-.968-.702.118-.162c.193-.264.456-.473.786-.625.327-.15.684-.225 1.068-.225.318 0 .602.053.85.16.255.11.456.267.6.47.146.206.22.44.22.698 0 .298-.083.58-.247.839-.158.25-.424.518-.797.807l-.653.506h1.422zm-3.063 3.7l.245-1.18h3.345l-.166.867-.06.11-.93.869a1.205 1.205 0 01.737 1.144c-.001.327-.098.622-.29.881a1.856 1.856 0 01-.774.593c-.322.139-.685.208-1.087.208-.328 0-.636-.045-.924-.135a2.06 2.06 0 01-.743-.4l-.132-.114.688-1.05.173.147c.12.103.267.185.442.245.177.06.364.091.562.091.251 0 .435-.044.554-.124a.31.31 0 00.146-.282c0-.086-.025-.135-.08-.171-.076-.05-.213-.079-.41-.079h-.687l.173-.88.06-.108.674-.632H2.516z"
                clip-rule="evenodd"
              ></path></svg
          ></span>
          <span class="own-btn-comment">
            <el-button class="btn" type="primary" round @click="commitComment"
              ><i class="el-icon-edit">
                {{ $t('m.Submit_Comment') }}</i
              ></el-button
            >
          </span>
        </div>
      </div>
      <h3 class="comment-total">
        <div class="text">
          <span>{{ $t('m.All_Comment') }}</span
          ><span class="number">{{ totalComment }}</span>
        </div>
      </h3>
      <div
        class="comment"
        id="commentbox"
        v-for="(item, commentIndex) in comments"
        :key="commentIndex"
        v-loading="loading"
      >
        <div class="info">
          <span
            @click="getInfoByUsername(item.fromUid, item.fromName)"
            class="user-info"
          >
            <avatar
              :username="item.fromName"
              :inline="true"
              :size="38"
              color="#FFF"
              :src="item.fromAvatar"
              :title="item.fromName"
            ></avatar>
          </span>
          <div class="right">
            <div class="name">
              <span
                style="margin-right:3px;vertical-align: middle;"
                class="user-info"
                @click="getInfoByUsername(item.fromUid, item.fromName)"
                :title="item.fromName"
                >{{ item.fromName }}</span
              >
              <span v-if="item.fromTitleName" style="margin-right: 4px;">
                <el-tag effect="dark" size="small" :color="item.fromTitleColor">
                  {{ item.fromTitleName }}
                </el-tag>
              </span>
              <span
                class="role-root role"
                title="Super Administrator"
                v-if="item.fromRole == 'root'"
                >SPA</span
              >
              <span
                class="role-admin role"
                title="Administrator"
                v-if="item.fromRole == 'admin'"
                >ADM</span
              >
            </div>
            <div class="date">
              <el-tooltip :content="item.gmtCreate | localtime" placement="top">
                <span>{{ item.gmtCreate | fromNow }}</span>
              </el-tooltip>
            </div>
          </div>
        </div>
        <div class="info-bottom">
          <div
            class="content markdown-content markdown-body"
            v-dompurify-html="mdToHtml(item.content)"
            v-katex
            v-highlight
          ></div>
          <div class="control">
            <span
              class="like"
              :class="{ active: commentLikeMap[item.id] == true }"
              @click="likeClick(item)"
            >
              <i class="iconfont fa fa-thumbs-o-up"></i>
              <span class="like-num">{{
                item.likeNum > 0 ? item.likeNum + $t('m.Like') : $t('m.Like')
              }}</span>
            </span>
            <span
              class="comment-opt comment-reply"
              @click="showCommentInput(item)"
              v-if="
                !cid || (cid && (item.fromUid == userInfo.uid || isContestAdmin || (item.fromRole != 'root' && item.fromUid != contest.uid)))
              "
            >
              <i class="iconfont el-icon-chat-square"></i>
              <span>{{ $t('m.Reply') }}</span>
            </span>
            <span
              v-if="item.fromUid == userInfo.uid || (!cid && isAdminRole) || (cid && isContestAdmin)"
              class="comment-opt comment-delete"
              @click="deleteComment(item, commentIndex)"
            >
              <i class="iconfont el-icon-delete"></i>
              <span>{{ $t('m.Delete') }}</span>
            </span>
          </div>
          <div class="reply">
            <div
              class="item"
              v-for="(reply, replyIndex) in item.replyList"
              :key="replyIndex"
            >
              <div class="reply-content">
                <span
                  class="from-name"
                  :title="reply.fromName"
                  @click="getInfoByUsername(reply.fromUid, reply.fromName)"
                >
                  <avatar
                    :username="reply.fromName"
                    :inline="true"
                    :size="25"
                    class="user-avatar"
                    color="#FFF"
                    :src="reply.fromAvatar"
                  ></avatar>
                  <span style="vertical-align: middle;">{{
                    reply.fromName
                  }}</span>
                </span>
                <span
                  v-if="reply.fromTitleName"
                  style="margin-right: 4px;"
                  class="hidden-xs-only"
                >
                  <el-tag
                    effect="dark"
                    size="small"
                    :color="reply.fromTitleColor"
                  >
                    {{ reply.fromTitleName }}
                  </el-tag>
                </span>
                <span
                  class="role-root role"
                  title="Super Administrator"
                  v-if="reply.fromRole == 'root'"
                  >SPA</span
                >
                <span
                  class="role-admin role"
                  title="Administrator"
                  v-if="reply.fromRole == 'admin'"
                  >ADM</span
                >
                <span class="reply-text">{{ $t('m.Reply') }}</span>
                <span
                  class="to-name"
                  :title="reply.toName"
                  @click="getInfoByUsername(reply.toUid, reply.toName)"
                  >@{{ reply.toName }}</span
                >
              </div>
              <div style="padding: 8px 0;margin-left: 34px;">
                <span
                  v-dompurify-html="mdToHtml(reply.content)"
                  class="markdown-content markdown-body"
                  v-katex
                  v-highlight
                ></span>
              </div>
              <div class="reply-bottom">
                <el-tooltip
                  :content="reply.gmtCreate | localtime"
                  placement="top"
                >
                  <span>{{ reply.gmtCreate | fromNow }}</span>
                </el-tooltip>

                <span
                  class="reply-opt reply-text"
                  @click="showCommentInput(item, reply)"
                >
                  <i class="iconfont el-icon-chat-square"></i>
                  <span>{{ $t('m.Reply') }}</span>
                </span>
                <span
                  class="reply-opt reply-delete"
                  v-if="reply.fromUid == userInfo.uid || (!cid && isAdminRole) || (cid && isContestAdmin)"
                  @click="deleteReply(reply, commentIndex, replyIndex)"
                >
                  <i class="iconfont el-icon-delete"></i>
                  <span>{{ $t('m.Delete') }}</span>
                </span>
              </div>
            </div>
            <div class="view-more item" v-if="item.totalReplyNum > 3">
              {{ $t('m.Reply_Total') }}<b> {{ item.totalReplyNum }} </b
              >{{ $t('m.Replies') }},
              <a
                class="btn-more"
                @click="showAllReply(item)"
                v-if="!item.hadOpen"
                >{{ $t('m.Click_Show_All') }}</a
              >
              <a class="btn-more" @click="pickWayReply(item)" v-else>{{
                $t('m.Pick_up')
              }}</a>
            </div>
            <transition name="fade">
              <div class="input-wrapper" v-if="showItemId === item.id">
                <el-input
                  v-model="replyInputComment"
                  type="textarea"
                  :rows="5"
                  autofocus
                  id="reply-textarea"
                  :placeholder="replyPlaceholder"
                >
                </el-input>
                <div class="input-bottom">
                  <span title="emoji">
                    <el-popover
                      placement="top-start"
                      width="300"
                      trigger="click"
                      v-model="replyFacelistVisiable"
                    >
                      <div class="emotionList">
                        <a
                          href="javascript:void(0);"
                          @click="getEmo(index, false)"
                          v-for="(item, index) in faceList"
                          :key="index"
                          class="emotionItem"
                          >{{ item }}</a
                        >
                      </div>
                      <i
                        class="fa fa-smile-o emotionSelect"
                        slot="reference"
                      ></i>
                    </el-popover>
                  </span>
                  <span
                    class="markdown-key"
                    :title="$t('m.Inline_Code')"
                    @click="addContentTips(0, true)"
                    ><svg
                      xmlns="http://www.w3.org/2000/svg"
                      viewBox="0 0 24 24"
                      width="1em"
                      height="1em"
                      fill="currentColor"
                      class="css-1rhb60f-Svg ea8ky5j0"
                    >
                      <path
                        fill-rule="evenodd"
                        d="M5.5 5h1.866a.5.5 0 01.486.617l-.96 4a.5.5 0 01-.486.383H5.5a.5.5 0 01-.5-.5v-4a.5.5 0 01.5-.5zm8 0h1.866a.5.5 0 01.486.617l-.96 4a.5.5 0 01-.486.383H13.5a.5.5 0 01-.5-.5v-4a.5.5 0 01.5-.5z"
                        clip-rule="evenodd"
                      ></path>
                    </svg>
                  </span>
                  <span
                    class="markdown-key"
                    :title="$t('m.Code_Block')"
                    @click="addContentTips(1, true)"
                  >
                    <svg
                      xmlns="http://www.w3.org/2000/svg"
                      viewBox="0 0 24 24"
                      width="1em"
                      height="1em"
                      fill="currentColor"
                      class="css-1rhb60f-Svg ea8ky5j0"
                    >
                      <path
                        fill-rule="evenodd"
                        d="M3.64 10.98c.147 0 .312-.022.495-.066.183-.044.348-.12.495-.23.147-.11.271-.269.374-.474.103-.205.154-.462.154-.77V5.084c0-.499.077-.928.231-1.287.154-.36.363-.66.627-.902s.565-.422.902-.539c.337-.117.69-.176 1.056-.176H9.58v1.848H8.37c-.425 0-.693.14-.803.418-.11.279-.165.58-.165.902v4.136c0 .425-.08.788-.242 1.09-.161.3-.352.545-.572.736-.22.19-.455.337-.704.44-.25.103-.462.161-.638.176v.044c.176.015.389.062.638.143.25.08.484.216.704.407.22.19.41.447.572.77.161.323.242.74.242 1.254v4.004c0 .323.055.623.165.902.11.279.378.418.803.418h1.21v1.848H7.974c-.367 0-.719-.059-1.056-.176a2.573 2.573 0 01-.902-.539 2.586 2.586 0 01-.627-.902c-.154-.36-.231-.788-.231-1.287V14.61c0-.352-.051-.638-.154-.858a1.494 1.494 0 00-.374-.517 1.18 1.18 0 00-.495-.253 2.143 2.143 0 00-.495-.066V10.98zm16.72 2.04c-.161 0-.33.022-.506.066a1.184 1.184 0 00-.484.253 1.494 1.494 0 00-.374.517c-.103.22-.154.506-.154.858v4.202c0 .499-.077.928-.231 1.287-.154.36-.363.66-.627.902a2.573 2.573 0 01-.902.54c-.337.116-.69.175-1.056.175H14.42v-1.848h1.21c.425 0 .693-.14.803-.418.11-.279.165-.58.165-.902v-4.004c0-.513.08-.931.242-1.254.161-.323.352-.58.572-.77.22-.19.455-.326.704-.407.25-.08.462-.128.638-.143v-.044a2.225 2.225 0 01-.638-.176 2.567 2.567 0 01-.704-.44c-.22-.19-.41-.436-.572-.737-.161-.3-.242-.664-.242-1.089V5.452c0-.323-.055-.623-.165-.902-.11-.279-.378-.418-.803-.418h-1.21V2.284h1.606c.367 0 .719.059 1.056.176.337.117.638.297.902.539.264.242.473.543.627.902.154.36.231.788.231 1.287v4.356c0 .308.051.565.154.77.103.205.227.363.374.473.147.11.308.187.484.231.176.044.345.066.506.066v1.936z"
                        clip-rule="evenodd"
                      ></path>
                    </svg>
                  </span>
                  <span
                    class="markdown-key"
                    :title="$t('m.Link')"
                    @click="addContentTips(2, true)"
                    ><svg
                      xmlns="http://www.w3.org/2000/svg"
                      viewBox="0 0 24 24"
                      width="1em"
                      height="1em"
                      fill="currentColor"
                      class="css-1rhb60f-Svg ea8ky5j0"
                    >
                      <path
                        fill-rule="evenodd"
                        d="M13 7a1 1 0 011-1h2a6 6 0 010 12h-2a1 1 0 110-2h2a4 4 0 000-8h-2a1 1 0 01-1-1zm-3 10a1 1 0 01-1 1H8A6 6 0 018 6h1a1 1 0 010 2H8a4 4 0 100 8h1a1 1 0 011 1zm-1-6h6a1 1 0 110 2H9a1 1 0 110-2z"
                        clip-rule="evenodd"
                      ></path></svg
                  ></span>
                  <span
                    class="markdown-key"
                    :title="$t('m.Unordered_list')"
                    @click="addContentTips(3, true)"
                    ><svg
                      xmlns="http://www.w3.org/2000/svg"
                      viewBox="0 0 24 24"
                      width="1em"
                      height="1em"
                      fill="currentColor"
                      class="css-1rhb60f-Svg ea8ky5j0"
                    >
                      <path
                        fill-rule="evenodd"
                        d="M8 7a1 1 0 110-2h13a1 1 0 110 2H8zm0 6a1 1 0 110-2h13a1 1 0 110 2H8zm0 6a1 1 0 110-2h13a1 1 0 110 2H8zM2.952 6.85a1.201 1.201 0 111.698-1.7 1.201 1.201 0 01-1.698 1.7zm0 6a1.201 1.201 0 111.698-1.7 1.201 1.201 0 01-1.698 1.7zm0 6a1.201 1.201 0 111.698-1.7 1.201 1.201 0 01-1.698 1.7z"
                        clip-rule="evenodd"
                      ></path></svg
                  ></span>
                  <span
                    class="markdown-key"
                    :title="$t('m.Ordered_List')"
                    @click="addContentTips(4, true)"
                    ><svg
                      xmlns="http://www.w3.org/2000/svg"
                      viewBox="0 0 24 24"
                      width="1em"
                      height="1em"
                      fill="currentColor"
                      class="css-1rhb60f-Svg ea8ky5j0"
                    >
                      <path
                        fill-rule="evenodd"
                        d="M7 6a1 1 0 011-1h13a1 1 0 110 2H8a1 1 0 01-1-1zm1 13a1 1 0 110-2h13a1 1 0 010 2H8zm0-6a1 1 0 110-2h13a1 1 0 010 2H8zM2.756 4.98l.236-1.18h2.22l-.92 4.6h-1.38l.684-3.42h-.84zm2.823 8.3l-.235 1.192H1.756l.174-.87.073-.117 1.911-1.498c.219-.178.368-.324.445-.434a.49.49 0 00.099-.287.208.208 0 00-.083-.18c-.067-.052-.176-.082-.335-.082a.897.897 0 00-.398.094 1.02 1.02 0 00-.342.272l-.12.144-.968-.702.118-.162c.193-.264.456-.473.786-.625.327-.15.684-.225 1.068-.225.318 0 .602.053.85.16.255.11.456.267.6.47.146.206.22.44.22.698 0 .298-.083.58-.247.839-.158.25-.424.518-.797.807l-.653.506h1.422zm-3.063 3.7l.245-1.18h3.345l-.166.867-.06.11-.93.869a1.205 1.205 0 01.737 1.144c-.001.327-.098.622-.29.881a1.856 1.856 0 01-.774.593c-.322.139-.685.208-1.087.208-.328 0-.636-.045-.924-.135a2.06 2.06 0 01-.743-.4l-.132-.114.688-1.05.173.147c.12.103.267.185.442.245.177.06.364.091.562.091.251 0 .435-.044.554-.124a.31.31 0 00.146-.282c0-.086-.025-.135-.08-.171-.076-.05-.213-.079-.41-.079h-.687l.173-.88.06-.108.674-.632H2.516z"
                        clip-rule="evenodd"
                      ></path></svg
                  ></span>
                  <span class="btn-control">
                    <el-button
                      class="btn"
                      type="danger"
                      round
                      @click="cancel"
                      size="small"
                      >{{ $t('m.Cancel') }}</el-button
                    >
                    <el-button
                      class="btn"
                      type="primary"
                      round
                      @click="commitReply(item.id)"
                      size="small"
                      >{{ $t('m.OK') }}</el-button
                    >
                  </span>
                </div>
              </div>
            </transition>
          </div>
        </div>
      </div>
    </div>
    <div class="container loading-text" v-if="showloading">
      <a style="background: #fff;padding:10px;" @click="loadMoreComment"
        ><span>{{ $t('m.Load_More') }}...</span></a
      >
    </div>
  </div>
</template>

<script>
import Avatar from 'vue-avatar';
import { mapGetters, mapState } from 'vuex';
import myMessage from '@/common/message';
import api from '@/common/api';
import { addCodeBtn } from '@/common/codeblock';
export default {
  props: {
    did: {
      type: Number,
      required: false,
      default: null,
    },
    cid: {
      type: Number,
      require: false,
      default: null,
    },
  },
  components: { Avatar },
  data() {
    return {
      replyInputComment: '',
      ownInputComment: '',
      showItemId: '',
      faceList: [],
      comments: [],
      commentLikeMap: {},
      facelistVisiable: false,
      replyFacelistVisiable: false,
      replyPlaceholder: '',
      query: {
        limit: 10,
        currentPage: 1,
        cid: null,
        did: null,
      },
      total: 0,
      totalComment: 0,
      replyObj: {
        commentId: 0,
        content: '',
        toUid: '',
        toName: '',
        toAvatar: '',
      },
      replyQuoteId: null,
      replyQuoteType: 'Comment',
      loading: false,
    };
  },

  created() {
    const appData = require('./emoji.json');
    for (let i in appData) {
      this.faceList.push(appData[i]);
    }
  },
  mounted() {
    this.query.did = this.did;
    this.query.cid = this.cid;
    this.init();
  },

  computed: {
    ...mapGetters(['userInfo', 'isAuthenticated', 'isAdminRole']),
    avatar() {
      return this.$store.getters.userInfo.avatar;
    },
  },
  methods: {
    init() {
      let queryParams = Object.assign({}, this.query);
      this.replyPlaceholder = this.$i18n.t(
        'm.Come_and_write_down_your_comments'
      );
      this.loading = true;
      api.getCommentList(queryParams).then(
        (res) => {
          let moreCommentList = res.data.data.commentList.records;
          for (let i = 0; i < moreCommentList.length; i++) {
            this.totalComment += 1 + moreCommentList[i].totalReplyNum;
          }
          this.comments = this.comments.concat(moreCommentList);
          this.total = res.data.data.commentList.total;
          this.commentLikeMap = res.data.data.commentLikeMap;
          if (this.comments.length > 0) {
            this.$nextTick((_) => {
              addCodeBtn();
            });
          }
          this.loading = false;
        },
        (err) => {
          this.loading = false;
        }
      );
    },

    /**
     * 点赞
     */
    likeClick(item) {
      // 没有赞过则表明想去进行点赞
      let toLike = this.commentLikeMap[item.id] != true;

      let sourceId = this.did;
      let sourceType = 'Discussion';

      if (this.cid != null) {
        sourceId = this.cid;
        sourceType = 'Contest';
      }

      api.toLikeComment(item.id, toLike, sourceId, sourceType).then((res) => {
        if (toLike) {
          this.commentLikeMap[item.id] = true;
          item.likeNum++;
        } else {
          item.likeNum--;
          this.commentLikeMap[item.id] = false;
        }
      });
    },

    /**
     * 点击取消按钮
     */
    cancel() {
      this.showItemId = '';
    },

    /**
     * 提交评论
     */
    commitComment() {
      if (!this.isAuthenticated) {
        myMessage.warning(this.$i18n.t('m.Please_login_first'));
        this.$store.dispatch('changeModalStatus', { visible: true });
        return;
      }
      if (this.ownInputComment.replace(/(^s*)|(s*$)/g, '').length == 0) {
        myMessage.warning(this.$i18n.t('m.Content_cannot_be_empty'));
        return;
      }
      if(this.ownInputComment.length > 10000){
        myMessage.error(this.$i18n.t("m.Comment_Content") + " " +this.$i18n.t("m.Can_not_exceed_10000"));
        return;
      }
      let comment = {
        content: this.ownInputComment,
        cid: this.cid,
        did: this.did,
      };
      api.addComment(comment).then((res) => {
        this.comments = [res.data.data].concat(this.comments);
        this.totalComment++;
        this.total++;
        myMessage.success(this.$i18n.t('m.Comment_Successfully'));
        this.ownInputComment = '';
        this.$nextTick((_) => {
          addCodeBtn();
        });
      });
    },

    /**
     * 提交回复
     */

    commitReply() {
      if (!this.isAuthenticated) {
        myMessage.warning(this.$i18n.t('m.Please_login_first'));
        this.$store.dispatch('changeModalStatus', { visible: true });
        return;
      }
      if (this.replyInputComment.replace(/(^s*)|(s*$)/g, '').length == 0) {
        myMessage.warning(this.$i18n.t('m.Content_cannot_be_empty'));
        return;
      }
      if(this.replyInputComment.length > 10000){
        myMessage.error(this.$i18n.t("m.Reply_Content") + " " +this.$i18n.t("m.Can_not_exceed_10000"));
        return;
      }
      this.replyObj.content = this.replyInputComment;
      let replyData = {
        reply: this.replyObj,
        did: this.did,
        quoteId: this.replyQuoteId,
        quoteType: this.replyQuoteType,
      };
      api.addReply(replyData).then((res) => {
        for (let i = 0; i < this.comments.length; i++) {
          if (this.comments[i].id == this.replyObj.commentId) {
            this.comments[i].replyList = [res.data.data].concat(
              this.comments[i].replyList
            );
            if (this.comments[i].backupReplyList) {
              this.comments[i].backupReplyList = [res.data.data].concat(
                this.comments[i].backupReplyList
              );
            }
            this.comments[i].totalReplyNum++;
            if (this.comments[i].totalReplyNum > 3) {
              this.comments[i].hadOpen = true;
            }
            break;
          }
        }
        this.$nextTick((_) => {
          addCodeBtn();
        });
        this.totalComment++;
        myMessage.success(this.$i18n.t('m.Reply_Successfully'));
        this.replyInputComment = '';
      });
    },

    /**
     * 点击评论按钮显示输入框
     * item: 当前大评论
     * reply: 当前回复的评论
     */
    showCommentInput(item, reply) {
      this.replyInputComment = '';
      if (reply) {
        this.replyPlaceholder = '@' + reply.fromName;
        // 对当前需要回复评论对象进行赋值
        this.replyObj.commentId = item.id;
        this.replyObj.toUid = reply.fromUid;
        this.replyObj.toName = reply.fromName;
        this.replyObj.toAvatar = reply.fromAvatar;
        this.replyQuoteId = reply.id;
        this.replyQuoteType = 'Reply';
      } else {
        this.replyPlaceholder = this.$i18n.t(
          'm.Come_and_write_down_your_comments'
        );
        this.replyObj.commentId = item.id;
        this.replyObj.toUid = item.fromUid;
        this.replyObj.toName = item.fromName;
        this.replyObj.toAvatar = item.fromAvatar;
        this.replyQuoteId = item.id;
        this.replyQuoteType = 'Comment';
      }
      this.showItemId = item.id;
    },

    /**
     * 删除评论及其回复
     */

    deleteComment(comment, commentIndex) {
      this.$confirm(this.$i18n.t('m.Delete_Comment_Tips'), 'Tips', {
        confirmButtonText: this.$i18n.t('m.OK'),
        cancelButtonText: this.$i18n.t('m.Cancel'),
        type: 'warning',
      })
        .then(() => {
          let commentDeleteData = {
            id: comment.id,
            fromUid: comment.fromUid,
            did: this.did,
            cid: this.cid
          };
          api.deleteComment(commentDeleteData).then((res) => {
            this.totalComment--;
            this.total--;
            this.totalComment -= comment.replyList.length;
            this.comments.splice(commentIndex, 1);
            myMessage.success(this.$i18n.t('m.Delete_successfully'));
          });
        })
        .catch(() => {});
    },

    /**
     * 删除回复
     */

    deleteReply(reply, commentIndex, replyIndex) {
      this.$confirm(this.$i18n.t('m.Delete_Reply_Tips'), 'Tips', {
        confirmButtonText: this.$i18n.t('m.OK'),
        cancelButtonText: this.$i18n.t('m.Cancel'),
        type: 'warning',
      })
        .then(() => {
          let replyDeleteData = {
            did: this.did,
            reply: reply,
          };
          api.deleteReply(replyDeleteData).then((res) => {
            myMessage.success(this.$i18n.t('m.Delete_successfully'));
            if (!this.comments[commentIndex].backupReplyList) {
              api
                .getAllReply(this.comments[commentIndex].id, this.cid)
                .then((res) => {
                  this.comments[commentIndex].backupReplyList = res.data.data;
                  if (res.data.data && res.data.data.length > 3) {
                    this.comments[commentIndex].replyList = res.data.data.slice(
                      0,
                      3
                    );
                  } else {
                    this.comments[commentIndex].replyList = res.data.data;
                  }
                  this.totalComment +=
                    res.data.data.length -
                    this.comments[commentIndex].totalReplyNum;
                  this.comments[commentIndex].totalReplyNum =
                    res.data.data.length;
                });
            } else {
              this.comments[commentIndex].backupReplyList.splice(replyIndex, 1);
              if (this.comments[commentIndex].backupReplyList.length > 3) {
                this.comments[commentIndex].replyList = this.comments[
                  commentIndex
                ].backupReplyList.slice(0, 3);
              } else {
                this.comments[commentIndex].replyList = this.comments[
                  commentIndex
                ].backupReplyList;
              }
              this.comments[commentIndex].totalReplyNum--;
              this.totalComment--;
            }
          });
        })
        .catch(() => {});
    },

    /**
     * 获取emoji表情写入到文本域中
     *
     */
    getEmo(index, isOwn) {
      var textArea;
      if (isOwn) {
        textArea = document.getElementById('own-textarea');
      } else {
        textArea = document.getElementById('reply-textarea');
      }
      function changeSelectedText(obj, str) {
        if (window.getSelection) {
          // 非IE浏览器
          textArea.setRangeText(str);
          // 在未选中文本的情况下，重新设置光标位置
          textArea.selectionStart += str.length;
          textArea.focus();
        } else if (document.selection) {
          // IE浏览器
          obj.focus();
          var sel = document.selection.createRange();
          sel.text = str;
        }
      }
      changeSelectedText(textArea, this.faceList[index]);
      if (isOwn) {
        this.ownInputComment = textArea.value; // 要同步data中的数据
        this.facelistVisiable = false; // 同时关闭表情列表
      } else {
        this.replyInputComment = textArea.value; // 要同步data中的数据
        this.replyFacelistVisiable = false; // 同时关闭表情列表
      }
      return;
    },

    addContentTips(num, isReply) {
      let tips = '';
      switch (num) {
        case 0:
          tips = '`your inline code...`';
          break;
        case 1:
          tips = '\n```\ncode block\n```\n';
          break;
        case 2:
          tips = '[HOJ](https://hcode.top)';
          break;
        case 3:
          tips = '\n- ...';
          break;
        case 4:
          tips = '\n1. ...';
          break;
      }
      if (isReply) {
        this.replyInputComment += tips;
      } else {
        this.ownInputComment += tips;
      }
    },

    mdToHtml(content) {
      return this.$markDown.render(content);
    },

    getInfoByUsername(uid, username) {
      this.$router.push({
        path: '/user-home',
        query: { uid, username },
      });
    },

    showAllReply(item) {
      if (!item.backupReplyList) {
        api.getAllReply(item.id, this.cid).then((res) => {
          item.replyList = res.data.data;
          item.backupReplyList = res.data.data;
          item.hadOpen = true;
          this.totalComment += res.data.data.length - item.totalReplyNum;
          item.totalReplyNum = res.data.data.length;
        });
      } else {
        item.replyList = item.backupReplyList;
        item.hadOpen = true;
      }
    },
    pickWayReply(item) {
      let newArr = [];
      for (let i = 0; i < 3; i++) {
        newArr.push(item.replyList[i]);
      }
      item.replyList = newArr;
      item.hadOpen = false;
      return;
    },
    loadMoreComment() {
      this.query.currentPage += 1;
      this.init();
    },
  },
  computed: {
    ...mapState({
      contest: (state) => state.contest.contest,
    }),
    ...mapGetters(['isAuthenticated', 'userInfo', 'isAdminRole','isContestAdmin']),
    showloading() {
      if (this.query.currentPage * this.query.limit >= this.total) {
        return false;
      } else {
        return true;
      }
    },
  },
  watch: {
    isAuthenticated(newVal, oldVal) {
      if (newVal != oldVal) {
        this.comments = [];
        this.init();
      }
    },
  },
};
</script>
<style>
.comment .markdown-content p {
  margin-top: 0 !important;
  margin-bottom: 0 !important;
}
</style>

<style scoped>
.comment-top {
  margin-bottom: 15px;
}
.comment-top .title {
  font-size: 20px;
  margin-left: 3.5em;
}
.comment-top p {
  margin: 5px;
  padding: 0;
}

.face-box {
  height: 200px !important;
  width: 300px !important;
  overflow: scroll !important;
  overflow-x: hidden !important;
}
.container {
  padding: 10px 20px;
  box-sizing: border-box;
  background-color: #fff;
  box-shadow: 0 2px 12px 0 rgb(0 0 0 / 10%);
  border: 1px solid #ebeef5;
  margin-bottom: 10px;
}
.container .own-input {
  margin-top: 10px;
}
.container .input-bottom {
  margin-top: 10px;
  padding: 0 10px;
}
.container .input-bottom .markdown-key {
  font-size: 20px;
  margin-left: 5px;
  cursor: pointer;
}
.container .own-input .own-btn-comment {
  float: right;
}
.container .emotionSelect {
  font-size: 25px;
  cursor: pointer;
}
.emotionList {
  display: flex;
  flex-wrap: wrap;
  padding: 5px;
  height: 200px;
  overflow-y: scroll;
}
.emotionItem {
  width: 10%;
  font-size: 20px;
  text-align: center;
}
/*包含以下四种的链接*/
.emotionItem {
  text-decoration: none;
}
/*正常的未被访问过的链接*/
.emotionItem:link {
  text-decoration: none;
}
/*已经访问过的链接*/
.emotionItem:visited {
  text-decoration: none;
}
/*鼠标划过(停留)的链接*/
.emotionItem:hover {
  text-decoration: none;
}
/* 正在点击的链接*/
.emotionItem:active {
  text-decoration: none;
}

.comment-total {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding-left: 12px;
  border-left: 4px solid #03a9f4;
  font-size: 18px;
  font-weight: 500;
  height: 20px;
  line-height: 20px;
}
.comment-total .text {
  display: flex;
  align-items: center;
}
.comment-total .number {
  margin-left: 6px;
  font-size: 14px;
  font-weight: normal;
}
.user-avatar {
  margin-right: 5px !important;
  vertical-align: middle;
}
.container .comment {
  display: flex;
  flex-direction: column;
  padding: 10px;
  border-top: 1px solid #eee;
}
.container .comment .info {
  display: flex;
  align-items: center;
}
.container .comment .info .user-info {
  cursor: pointer;
}
.container .comment .info .right {
  display: flex;
  flex-direction: column;
  margin-left: 10px;
}
.container .comment .info .right .name {
  font-size: 16px;
  color: #409eff;
  margin-bottom: 5px;
  font-weight: 500;
}
.container .comment .info .right .date {
  font-size: 12px;
  color: #909399;
}
.container .comment .info-bottom {
  margin-left: 47px;
}
.container .comment .content {
  font-size: 16px;
  color: #303133;
  line-height: 20px;
  padding: 10px 0;
}
.container .comment .control {
  display: flex;
  align-items: center;
  font-size: 14px;
  color: #909399;
}
.container .comment .control .like {
  display: flex;
  align-items: center;
  margin-right: 10px;
  cursor: pointer;
}
.container .comment .control .like.active,
.container .comment .control .like:hover {
  color: #409eff;
}
.container .comment .control .like .iconfont {
  font-size: 14px;
  margin-right: 3px;
}
.container .comment .control .comment-opt {
  display: flex;
  align-items: center;
  cursor: pointer;
  margin-right: 10px;
}
.container .comment .control .comment-reply:hover {
  color: #333;
}

.container .comment .control .comment-delete:hover {
  color: #ff503f;
}

.container .comment .control .comment-opt .iconfont {
  font-size: 16px;
  margin-right: 3px;
}
.container .comment .reply {
  margin: 10px 0;
  border-left: 2px solid #dcdfe6;
}
.container .comment .reply .item {
  margin: 0 10px;
  padding: 10px 0;
  border-bottom: 1px dashed #ebeef5;
}
.container .comment .reply .item .reply-content {
  display: flex;
  align-items: center;
  font-size: 14px;
  color: #303133;
}
.container .comment .reply .item .reply-content .from-name {
  color: #409eff;
  cursor: pointer;
  margin-right: 3px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.container .comment .reply .item .reply-content .reply-text {
  margin-left: 5px;
  margin-right: 2px;
  color: #333;
  font-size: 14px;
  font-weight: normal;
  width: 32px;
}
.container .comment .reply .item .reply-content .to-name {
  color: #409eff;
  margin-right: 5px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  cursor: pointer;
}
.container .comment .reply .item .reply-bottom {
  display: flex;
  align-items: center;
  font-size: 12px;
  color: #909399;
  margin-left: 34px;
}
.container .comment .reply .item .reply-bottom .reply-opt {
  display: flex;
  align-items: center;
  margin-left: 5px;
  cursor: pointer;
}
.container .comment .reply .item .reply-bottom .reply-text:hover {
  color: #333;
}
.container .comment .reply .item .reply-bottom .reply-delete:hover {
  color: #ff503f;
}
.container .comment .reply .item .reply-bottom .reply-opt .iconfont {
  margin-right: 3px;
}
.container .comment .reply .view-more {
  font-size: 12px;
  color: #6d757a;
}
.container .comment .reply .view-more .btn-more {
  padding: 2px 3px;
  border-radius: 4px;
}
.container .comment .reply .view-more a {
  outline: none;
  color: #00a1d6;
  text-decoration: none;
  cursor: pointer;
}
.container .comment .reply .view-more a:hover {
  background: #e5e9ef;
  color: #00a1d6;
}

.container .comment .reply .fade-enter-active,
.container .comment .reply fade-leave-active {
  transition: opacity 0.5s;
}
.container .comment .reply .fade-enter,
.container .comment .reply .fade-leave-to {
  opacity: 0;
}
.container .comment .reply .input-wrapper {
  padding: 10px;
}
.container .btn-control {
  float: right;
  align-items: center;
}
.container .comment .reply .input-wrapper .btn-control .cancel {
  font-size: 16px;
  color: #606266;
  margin-right: 20px;
  cursor: pointer;
}
.container .comment .reply .input-wrapper .btn-control .cancel:hover {
  color: #333;
}
.container .comment .reply .input-wrapper .btn-control .confirm {
  font-size: 16px;
}
.loading-text {
  text-align: center;
}
.loading-text a {
  color: #999;
}
.loading-text a:hover {
  text-decoration: none;
  color: #03a9f4;
}
</style>
