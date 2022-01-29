package top.hcode.hoj.crawler.language;

import top.hcode.hoj.pojo.entity.problem.Language;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * @Author: Himit_ZH
 * @Date: 2022/1/27 21:14
 * @Description:
 */
public abstract class LanguageStrategy {


    public abstract String getLanguageNameById(String id);

    public abstract List<Language> buildLanguageListByIds(List<Language> allLanguageList, List<String> langIdList);

    public abstract Collection<String> getLangList();

    public abstract String getOJName();

    public List<Language> buildLanguageList() {
        List<Language> languageList = new ArrayList<>();
        for (String lang : getLangList()) {
            languageList.add(new Language()
                    .setName(lang)
                    .setDescription(lang)
                    .setOj(getOJName())
                    .setContentType(getLangContentType(lang)));
        }
        return languageList;
    }

    private final static List<String> CLang = Arrays.asList("c", "gcc", "clang");
    private final static List<String> CPPLang = Arrays.asList("c++", "g++", "clang++");
    private final static List<String> PythonLang = Arrays.asList("python", "pypy");
    private final static List<String> JSLang = Arrays.asList("node", "javascript");

    protected String getLangContentType(String name) {
        String lowerName = name.toLowerCase();

        for (String lang : CPPLang) {
            if (lowerName.contains(lang)) {
                return "text/x-c++src";
            }
        }

        if (lowerName.contains("c#")) {
            return "text/x-csharp";
        }

        for (String lang : CLang) {
            if (lowerName.contains(lang)) {
                return "text/x-csrc";
            }
        }

        for (String lang : PythonLang) {
            if (lowerName.contains(lang)) {
                return "text/x-python";
            }
        }
        for (String lang : JSLang) {
            if (lowerName.contains(lang)) {
                return "text/javascript";
            }
        }
        if (lowerName.contains("scala")) {
            return "text/x-scala";
        }

        if (lowerName.contains("java")) {
            return "text/x-java";
        }

        if (lowerName.contains("pascal")) {
            return "text/x-pascal";
        }

        if (lowerName.contains("go")) {
            return "text/x-go";
        }

        if (lowerName.contains("ruby")) {
            return "text/x-ruby";
        }

        if (lowerName.contains("rust")) {
            return "text/x-rustsrc";
        }

        if (lowerName.contains("php")) {
            return "text/x-php";
        }

        if (lowerName.contains("perl")) {
            return "text/x-perl";
        }

        if (lowerName.contains("fortran")) {
            return "text/x-fortran";
        }

        if (lowerName.contains("haskell")) {
            return "text/x-haskell";
        }

        if (lowerName.contains("ocaml")) {
            return "text/x-ocaml";
        }

        return null;

    }
}