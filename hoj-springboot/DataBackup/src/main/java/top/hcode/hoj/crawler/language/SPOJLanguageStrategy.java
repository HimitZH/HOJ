package top.hcode.hoj.crawler.language;

import cn.hutool.core.util.ReUtil;
import cn.hutool.http.HttpUtil;
import top.hcode.hoj.pojo.entity.problem.Language;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @Author: Himit_ZH
 * @Date: 2022/1/27 21:21
 * @Description:
 */
public class SPOJLanguageStrategy extends LanguageStrategy {

    private static final HashMap<String, String> languageMap = new HashMap<>();

    static {
        languageMap.put("7", "Ada95 (gnat 8.3)");
        languageMap.put("45", "Assembler 32 (gcc 8.3)");
        languageMap.put("13", "Assembler 32 (nasm 2.14)");
        languageMap.put("42", "Assembler 64 (nasm 2.14)");
        languageMap.put("104", "AWK (gawk 4.2.1)");
        languageMap.put("105", "AWK (mawk 1.3.3)");
        languageMap.put("28", "Bash (bash 5.0.3)");
        languageMap.put("110", "BC (bc 1.07.1)");
        languageMap.put("12", "Brainf**k (bff 1.0.6)");
        languageMap.put("81", "C (clang 8.0)");
        languageMap.put("11", "C (gcc 8.3)");
        languageMap.put("27", "C# (gmcs 5.20.1)");
        languageMap.put("41", "C++ (g++ 4.3.2)");
        languageMap.put("1", "C++ (gcc 8.3)");
        languageMap.put("44", "C++14 (gcc 8.3)");
        languageMap.put("82", "C++14 (clang 8.0)");
        languageMap.put("34", "C99 (gcc 8.3)");
        languageMap.put("14", "Clips (clips 6.30)");
        languageMap.put("111", "Clojure (clojure 1.10.0)");
        languageMap.put("118", "Cobol (gnucobol 2.2.0)");
        languageMap.put("91", "CoffeeScript (coffee 2.4.1)");
        languageMap.put("31", "Common Lisp (sbcl 1.4.16)");
        languageMap.put("32", "Common Lisp (clisp 2.49.92)");
        languageMap.put("102", "D (dmd 2.085.0)");
        languageMap.put("84", "D (ldc 1.12.0)");
        languageMap.put("20", "D (gdc 8.3)");
        languageMap.put("48", "Dart (dart 2.3.0)");
        languageMap.put("96", "Elixir (elixir 1.8.2)");
        languageMap.put("36", "Erlang (erl 21.3.8)");
        languageMap.put("124", "F# (mono 4.1)");
        languageMap.put("92", "Fantom (fantom 1.0.72)");
        languageMap.put("107", "Forth (gforth 0.7.3)");
        languageMap.put("5", "Fortran (gfortran 8.3)");
        languageMap.put("114", "Go (go 1.12.1)");
        languageMap.put("98", "Gosu (gosu 1.14.9)");
        languageMap.put("121", "Groovy (groovy 2.5.6)");
        languageMap.put("21", "Haskell (ghc 8.4.4)");
        languageMap.put("16", "Icon (iconc 9.5.1)");
        languageMap.put("9", "Intercal (ick 0.3)");
        languageMap.put("24", "JAR (JavaSE 6)");
        languageMap.put("10", "Java (HotSpot 12)");
        languageMap.put("35", "JavaScript (rhino 1.7.9)");
        languageMap.put("112", "JavaScript (SMonkey 60.2.3)");
        languageMap.put("47", "Kotlin (kotlin 1.3.21)");
        languageMap.put("26", "Lua (luac 5.3.3)");
        languageMap.put("30", "Nemerle (ncc 1.2.547)");
        languageMap.put("25", "Nice (nicec 0.9.13)");
        languageMap.put("122", "Nim (nim 0.19.4)");
        languageMap.put("56", "Node.js (node 11.12.0)");
        languageMap.put("43", "Objective-C (gcc 8.3)");
        languageMap.put("83", "Objective-C (clang 8.0)");
        languageMap.put("8", "Ocaml (ocamlopt 4.05.0)");
        languageMap.put("127", "Octave (octave 4.4.1)");
        languageMap.put("2", "Pascal (gpc 20070904)");
        languageMap.put("22", "Pascal (fpc 3.0.4)");
        languageMap.put("54", "Perl (perl 2018.12)");
        languageMap.put("3", "Perl (perl 5.28.1)");
        languageMap.put("29", "PHP (php 7.3.5)");
        languageMap.put("94", "Pico Lisp (pico 18.12.27)");
        languageMap.put("19", "Pike (pike 8.0)");
        languageMap.put("15", "Prolog (swi 7.6.4)");
        languageMap.put("108", "Prolog (gprolog 1.4.5)");
        languageMap.put("4", "Python (cpython 2.7.16)");
        languageMap.put("99", "Python (PyPy 2.7.13)");
        languageMap.put("116", "Python 3 (python  3.7.3)");
        languageMap.put("126", "Python 3 nbc (python 3.7.3)");
        languageMap.put("117", "R (R 3.5.2)");
        languageMap.put("95", "Racket (racket 7.0)");
        languageMap.put("17", "Ruby (ruby 2.5.5)");
        languageMap.put("93", "Rust (rust 1.33.0)");
        languageMap.put("39", "Scala (scala 2.12.8)");
        languageMap.put("18", "Scheme (stalin 0.11)");
        languageMap.put("33", "Scheme (guile 2.2.4)");
        languageMap.put("97", "Scheme (chicken 4.13)");
        languageMap.put("46", "Sed (sed 4.7)");
        languageMap.put("23", "Smalltalk (gst 3.2.5)");
        languageMap.put("40", "SQLite (sqlite 3.27.2)");
        languageMap.put("85", "Swift (swift 4.2.2)");
        languageMap.put("38", "TCL (tcl 8.6)");
        languageMap.put("62", "Text (plain text)");
        languageMap.put("115", "Unlambda (unlambda 0.1.4.2)");
        languageMap.put("50", "VB.net (mono 4.7)");
        languageMap.put("6", "Whitespace (wspace 0.3)");
    }


    @Override
    public String getLanguageNameById(String id) {
        return languageMap.get(id);
    }

    @Override
    public List<Language> buildLanguageListByIds(List<Language> allLanguageList, List<String> langIdList) {

        List<String> langNameList = langIdList.stream().map(this::getLanguageNameById).collect(Collectors.toList());

        return allLanguageList.stream().filter(language -> langNameList.contains(language.getName())).collect(Collectors.toList());
    }

    @Override
    public Collection<String> getLangList() {
        return languageMap.values();
    }

    @Override
    public String getOJName() {
        return "SPOJ";
    }

    public static void main(String[] args) {
        String url = "https://www.spoj.com/submit/HOTLINE/";
        String body = HttpUtil.get(url);
        Pattern pattern1 = Pattern.compile("<option value=\"([\\s\\S]*?)\" >[\\s\\S]*?</option>");
        Pattern pattern2 = Pattern.compile("<option value=\"[\\s\\S]*?\" >([\\s\\S]*?)</option>");
        List<String> allGroups1 = ReUtil.findAll(pattern1, body, 1);
        List<String> allGroups2 = ReUtil.findAll(pattern2, body, 1);
        for (int i = 0; i < allGroups1.size(); i++) {
            System.out.println("languageMap.put(\"" + allGroups2.get(i) + "\", \"" + allGroups1.get(i) + "\");");
        }
    }
}