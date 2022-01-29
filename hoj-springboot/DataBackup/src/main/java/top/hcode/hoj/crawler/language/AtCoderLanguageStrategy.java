package top.hcode.hoj.crawler.language;

import cn.hutool.core.util.ReUtil;
import top.hcode.hoj.pojo.entity.problem.Language;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @Author: Himit_ZH
 * @Date: 2022/1/28 21:48
 * @Description:
 */
public class AtCoderLanguageStrategy extends LanguageStrategy {

    @Override
    public String getLanguageNameById(String id) {
        return null;
    }

    @Override
    public List<Language> buildLanguageListByIds(List<Language> allLanguageList, List<String> langIdList) {
        return null;
    }

    @Override
    public Collection<String> getLangList() {
        HashMap<String, String> languageMap = new HashMap<>();
        languageMap.put("C (GCC 9.2.1)", "4001");
        languageMap.put("C (Clang 10.0.0)", "4002");
        languageMap.put("C++ (GCC 9.2.1)", "4003");
        languageMap.put("C++ (Clang 10.0.0)", "4004");
        languageMap.put("Java (OpenJDK 11.0.6)", "4005");
        languageMap.put("Python (3.8.2)", "4006");
        languageMap.put("Bash (5.0.11)", "4007");
        languageMap.put("bc (1.07.1)", "4008");
        languageMap.put("Awk (GNU Awk 4.1.4)", "4009");
        languageMap.put("C# (.NET Core 3.1.201)", "4010");
        languageMap.put("C# (Mono-mcs 6.8.0.105)", "4011");
        languageMap.put("C# (Mono-csc 3.5.0)", "4012");
        languageMap.put("Clojure (1.10.1.536)", "4013");
        languageMap.put("Crystal (0.33.0)", "4014");
        languageMap.put("D (DMD 2.091.0)", "4015");
        languageMap.put("D (GDC 9.2.1)", "4016");
        languageMap.put("D (LDC 1.20.1)", "4017");
        languageMap.put("Dart (2.7.2)", "4018");
        languageMap.put("dc (1.4.1)", "4019");
        languageMap.put("Erlang (22.3)", "4020");
        languageMap.put("Elixir (1.10.2)", "4021");
        languageMap.put("F# (.NET Core 3.1.201)", "4022");
        languageMap.put("F# (Mono 10.2.3)", "4023");
        languageMap.put("Forth (gforth 0.7.3)", "4024");
        languageMap.put("Fortran (GNU Fortran 9.2.1)", "4025");
        languageMap.put("Go (1.14.1)", "4026");
        languageMap.put("Haskell (GHC 8.8.3)", "4027");
        languageMap.put("Haxe (4.0.3); js", "4028");
        languageMap.put("Haxe (4.0.3); Java", "4029");
        languageMap.put("JavaScript (Node.js 12.16.1)", "4030");
        languageMap.put("Julia (1.4.0)", "4031");
        languageMap.put("Kotlin (1.3.71)", "4032");
        languageMap.put("Lua (Lua 5.3.5)", "4033");
        languageMap.put("Lua (LuaJIT 2.1.0)", "4034");
        languageMap.put("Dash (0.5.8)", "4035");
        languageMap.put("Nim (1.0.6)", "4036");
        languageMap.put("Objective-C (Clang 10.0.0)", "4037");
        languageMap.put("Common Lisp (SBCL 2.0.3)", "4038");
        languageMap.put("OCaml (4.10.0)", "4039");
        languageMap.put("Octave (5.2.0)", "4040");
        languageMap.put("Pascal (FPC 3.0.4)", "4041");
        languageMap.put("Perl (5.26.1)", "4042");
        languageMap.put("Raku (Rakudo 2020.02.1)", "4043");
        languageMap.put("PHP (7.4.4)", "4044");
        languageMap.put("Prolog (SWI-Prolog 8.0.3)", "4045");
        languageMap.put("PyPy2 (7.3.0)", "4046");
        languageMap.put("PyPy3 (7.3.0)", "4047");
        languageMap.put("Racket (7.6)", "4048");
        languageMap.put("Ruby (2.7.1)", "4049");
        languageMap.put("Rust (1.42.0)", "4050");
        languageMap.put("Scala (2.13.1)", "4051");
        languageMap.put("Java (OpenJDK 1.8.0)", "4052");
        languageMap.put("Scheme (Gauche 0.9.9)", "4053");
        languageMap.put("Standard ML (MLton 20130715)", "4054");
        languageMap.put("Swift (5.2.1)", "4055");
        languageMap.put("Text (cat 8.28)", "4056");
        languageMap.put("TypeScript (3.8)", "4057");
        languageMap.put("Visual Basic (.NET Core 3.1.101)", "4058");
        languageMap.put("Zsh (5.4.2)", "4059");
        languageMap.put("COBOL - Fixed (OpenCOBOL 1.1.0)", "4060");
        languageMap.put("COBOL - Free (OpenCOBOL 1.1.0)", "4061");
        languageMap.put("Brainfuck (bf 20041219)", "4062");
        languageMap.put("Ada2012 (GNAT 9.2.1)", "4063");
        languageMap.put("Unlambda (2.0.0)", "4064");
        languageMap.put("Cython (0.29.16)", "4065");
        languageMap.put("Sed (4.4)", "4066");
        languageMap.put("Vim (8.2.0460)", "4067");
        return languageMap.keySet();
    }

    @Override
    public String getOJName() {
        return "AC";
    }

    @Override
    protected String getLangContentType(String name) {
        HashMap<String, String> nameMapContestType = new HashMap<>();
        nameMapContestType.put("C (GCC 9.2.1)", "text/x-csrc");
        nameMapContestType.put("C (Clang 10.0.0)", "text/x-csrc");
        nameMapContestType.put("C++ (GCC 9.2.1)", "text/x-c++src");
        nameMapContestType.put("C++ (Clang 10.0.0)", "text/x-c++src");
        nameMapContestType.put("Java (OpenJDK 11.0.6)", "text/x-java");
        nameMapContestType.put("Python (3.8.2)", "text/x-python");
        nameMapContestType.put("Bash (5.0.11)", "text/x-sh");
        nameMapContestType.put("bc (1.07.1)", "text/x-bc");
        nameMapContestType.put("Awk (GNU Awk 4.1.4)", "text/x-sh");
        nameMapContestType.put("C# (.NET Core 3.1.201)", "text/x-csharp");
        nameMapContestType.put("C# (Mono-mcs 6.8.0.105)", "text/x-csharp");
        nameMapContestType.put("C# (Mono-csc 3.5.0)", "text/x-csharp");
        nameMapContestType.put("Clojure (1.10.1.536)", "text/x-clojure");
        nameMapContestType.put("Crystal (0.33.0)", "text/x-crystal");
        nameMapContestType.put("D (DMD 2.091.0)", "text/x-d");
        nameMapContestType.put("D (GDC 9.2.1)", "text/x-d");
        nameMapContestType.put("D (LDC 1.20.1)", "text/x-d");
        nameMapContestType.put("Dart (2.7.2)", "application/dart");
        nameMapContestType.put("dc (1.4.1)", "text/x-dc");
        nameMapContestType.put("Erlang (22.3)", "text/x-erlang");
        nameMapContestType.put("Elixir (1.10.2)", "elixir");
        nameMapContestType.put("F# (.NET Core 3.1.201)", "text/x-fsharp");
        nameMapContestType.put("F# (Mono 10.2.3)", "text/x-fsharp");
        nameMapContestType.put("Forth (gforth 0.7.3)", "text/x-forth");
        nameMapContestType.put("Fortran (GNU Fortran 9.2.1)", "text/x-fortran");
        nameMapContestType.put("Go (1.14.1)", "text/x-go");
        nameMapContestType.put("Haskell (GHC 8.8.3)", "text/x-haskell");
        nameMapContestType.put("Haxe (4.0.3); js", "text/x-haxe");
        nameMapContestType.put("Haxe (4.0.3); Java", "text/x-haxe");
        nameMapContestType.put("JavaScript (Node.js 12.16.1)", "text/javascript");
        nameMapContestType.put("Julia (1.4.0)", "text/x-julia");
        nameMapContestType.put("Kotlin (1.3.71)", "text/x-kotlin");
        nameMapContestType.put("Lua (Lua 5.3.5)", "text/x-lua");
        nameMapContestType.put("Lua (LuaJIT 2.1.0)", "text/x-lua");
        nameMapContestType.put("Dash (0.5.8)", "text/x-sh");
        nameMapContestType.put("Nim (1.0.6)", "text/x-nim");
        nameMapContestType.put("Objective-C (Clang 10.0.0)", "text/x-objectivec");
        nameMapContestType.put("Common Lisp (SBCL 2.0.3)", "text/x-common-lisp");
        nameMapContestType.put("OCaml (4.10.0)", "text/x-ocaml");
        nameMapContestType.put("Octave (5.2.0)", "text/x-octave");
        nameMapContestType.put("Pascal (FPC 3.0.4)", "text/x-pascal");
        nameMapContestType.put("Perl (5.26.1)", "text/x-perl");
        nameMapContestType.put("Raku (Rakudo 2020.02.1)", "text/x-perl");
        nameMapContestType.put("PHP (7.4.4)", "text/x-php");
        nameMapContestType.put("Prolog (SWI-Prolog 8.0.3)", "text/x-prolog");
        nameMapContestType.put("PyPy2 (7.3.0)", "text/x-python");
        nameMapContestType.put("PyPy3 (7.3.0)", "text/x-python");
        nameMapContestType.put("Racket (7.6)", "text/x-racket");
        nameMapContestType.put("Ruby (2.7.1)", "text/x-ruby");
        nameMapContestType.put("Rust (1.42.0)", "text/x-rustsrc");
        nameMapContestType.put("Scala (2.13.1)", "text/x-scala");
        nameMapContestType.put("Java (OpenJDK 1.8.0)", "text/x-java");
        nameMapContestType.put("Scheme (Gauche 0.9.9)", "text/x-scheme");
        nameMapContestType.put("Standard ML (MLton 20130715)", "text/x-sml");
        nameMapContestType.put("Swift (5.2.1)", "text/x-swift");
        nameMapContestType.put("Text (cat 8.28)", "text/plain");
        nameMapContestType.put("TypeScript (3.8)", "text/typescript");
        nameMapContestType.put("Visual Basic (.NET Core 3.1.101)", "text/x-vb");
        nameMapContestType.put("Zsh (5.4.2)", "text/x-sh");
        nameMapContestType.put("COBOL - Fixed (OpenCOBOL 1.1.0)", "text/x-cobol");
        nameMapContestType.put("COBOL - Free (OpenCOBOL 1.1.0)", "text/x-cobol");
        nameMapContestType.put("Brainfuck (bf 20041219)", "text/x-brainfuck");
        nameMapContestType.put("Ada2012 (GNAT 9.2.1)", "text/x-ada");
        nameMapContestType.put("Unlambda (2.0.0)", "text/x-unlambda");
        nameMapContestType.put("Cython (0.29.16)", "text/x-python");
        nameMapContestType.put("Sed (4.4)", "text/x-sh");
        nameMapContestType.put("Vim (8.2.0460)", "text/x-vim");
        return nameMapContestType.get(name);
    }

    public static void main(String[] args) {
        String body = "<option value=\"4001\" data-mime=\"text/x-csrc\">C (GCC 9.2.1)</option>\n" +
                "\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\t<option value=\"4002\" data-mime=\"text/x-csrc\">C (Clang 10.0.0)</option>\n" +
                "\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\t<option value=\"4003\" data-mime=\"text/x-c++src\">C++ (GCC 9.2.1)</option>\n" +
                "\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\t<option value=\"4004\" data-mime=\"text/x-c++src\">C++ (Clang 10.0.0)</option>\n" +
                "\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\t<option value=\"4005\" data-mime=\"text/x-java\">Java (OpenJDK 11.0.6)</option>\n" +
                "\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\t<option value=\"4006\" data-mime=\"text/x-python\">Python (3.8.2)</option>\n" +
                "\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\t<option value=\"4007\" data-mime=\"text/x-sh\">Bash (5.0.11)</option>\n" +
                "\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\t<option value=\"4008\" data-mime=\"text/x-bc\">bc (1.07.1)</option>\n" +
                "\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\t<option value=\"4009\" data-mime=\"text/x-sh\">Awk (GNU Awk 4.1.4)</option>\n" +
                "\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\t<option value=\"4010\" data-mime=\"text/x-csharp\">C# (.NET Core 3.1.201)</option>\n" +
                "\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\t<option value=\"4011\" data-mime=\"text/x-csharp\">C# (Mono-mcs 6.8.0.105)</option>\n" +
                "\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\t<option value=\"4012\" data-mime=\"text/x-csharp\">C# (Mono-csc 3.5.0)</option>\n" +
                "\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\t<option value=\"4013\" data-mime=\"text/x-clojure\">Clojure (1.10.1.536)</option>\n" +
                "\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\t<option value=\"4014\" data-mime=\"text/x-crystal\">Crystal (0.33.0)</option>\n" +
                "\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\t<option value=\"4015\" data-mime=\"text/x-d\">D (DMD 2.091.0)</option>\n" +
                "\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\t<option value=\"4016\" data-mime=\"text/x-d\">D (GDC 9.2.1)</option>\n" +
                "\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\t<option value=\"4017\" data-mime=\"text/x-d\">D (LDC 1.20.1)</option>\n" +
                "\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\t<option value=\"4018\" data-mime=\"application/dart\">Dart (2.7.2)</option>\n" +
                "\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\t<option value=\"4019\" data-mime=\"text/x-dc\">dc (1.4.1)</option>\n" +
                "\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\t<option value=\"4020\" data-mime=\"text/x-erlang\">Erlang (22.3)</option>\n" +
                "\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\t<option value=\"4021\" data-mime=\"elixir\">Elixir (1.10.2)</option>\n" +
                "\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\t<option value=\"4022\" data-mime=\"text/x-fsharp\">F# (.NET Core 3.1.201)</option>\n" +
                "\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\t<option value=\"4023\" data-mime=\"text/x-fsharp\">F# (Mono 10.2.3)</option>\n" +
                "\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\t<option value=\"4024\" data-mime=\"text/x-forth\">Forth (gforth 0.7.3)</option>\n" +
                "\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\t<option value=\"4025\" data-mime=\"text/x-fortran\">Fortran (GNU Fortran 9.2.1)</option>\n" +
                "\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\t<option value=\"4026\" data-mime=\"text/x-go\">Go (1.14.1)</option>\n" +
                "\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\t<option value=\"4027\" data-mime=\"text/x-haskell\">Haskell (GHC 8.8.3)</option>\n" +
                "\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\t<option value=\"4028\" data-mime=\"text/x-haxe\">Haxe (4.0.3); js</option>\n" +
                "\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\t<option value=\"4029\" data-mime=\"text/x-haxe\">Haxe (4.0.3); Java</option>\n" +
                "\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\t<option value=\"4030\" data-mime=\"text/javascript\">JavaScript (Node.js 12.16.1)</option>\n" +
                "\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\t<option value=\"4031\" data-mime=\"text/x-julia\">Julia (1.4.0)</option>\n" +
                "\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\t<option value=\"4032\" data-mime=\"text/x-kotlin\">Kotlin (1.3.71)</option>\n" +
                "\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\t<option value=\"4033\" data-mime=\"text/x-lua\">Lua (Lua 5.3.5)</option>\n" +
                "\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\t<option value=\"4034\" data-mime=\"text/x-lua\">Lua (LuaJIT 2.1.0)</option>\n" +
                "\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\t<option value=\"4035\" data-mime=\"text/x-sh\">Dash (0.5.8)</option>\n" +
                "\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\t<option value=\"4036\" data-mime=\"text/x-nim\">Nim (1.0.6)</option>\n" +
                "\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\t<option value=\"4037\" data-mime=\"text/x-objectivec\">Objective-C (Clang 10.0.0)</option>\n" +
                "\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\t<option value=\"4038\" data-mime=\"text/x-common-lisp\">Common Lisp (SBCL 2.0.3)</option>\n" +
                "\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\t<option value=\"4039\" data-mime=\"text/x-ocaml\">OCaml (4.10.0)</option>\n" +
                "\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\t<option value=\"4040\" data-mime=\"text/x-octave\">Octave (5.2.0)</option>\n" +
                "\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\t<option value=\"4041\" data-mime=\"text/x-pascal\">Pascal (FPC 3.0.4)</option>\n" +
                "\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\t<option value=\"4042\" data-mime=\"text/x-perl\">Perl (5.26.1)</option>\n" +
                "\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\t<option value=\"4043\" data-mime=\"text/x-perl\">Raku (Rakudo 2020.02.1)</option>\n" +
                "\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\t<option value=\"4044\" data-mime=\"text/x-php\">PHP (7.4.4)</option>\n" +
                "\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\t<option value=\"4045\" data-mime=\"text/x-prolog\">Prolog (SWI-Prolog 8.0.3)</option>\n" +
                "\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\t<option value=\"4046\" data-mime=\"text/x-python\">PyPy2 (7.3.0)</option>\n" +
                "\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\t<option value=\"4047\" data-mime=\"text/x-python\">PyPy3 (7.3.0)</option>\n" +
                "\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\t<option value=\"4048\" data-mime=\"text/x-racket\">Racket (7.6)</option>\n" +
                "\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\t<option value=\"4049\" data-mime=\"text/x-ruby\">Ruby (2.7.1)</option>\n" +
                "\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\t<option value=\"4050\" data-mime=\"text/x-rustsrc\">Rust (1.42.0)</option>\n" +
                "\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\t<option value=\"4051\" data-mime=\"text/x-scala\">Scala (2.13.1)</option>\n" +
                "\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\t<option value=\"4052\" data-mime=\"text/x-java\">Java (OpenJDK 1.8.0)</option>\n" +
                "\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\t<option value=\"4053\" data-mime=\"text/x-scheme\">Scheme (Gauche 0.9.9)</option>\n" +
                "\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\t<option value=\"4054\" data-mime=\"text/x-sml\">Standard ML (MLton 20130715)</option>\n" +
                "\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\t<option value=\"4055\" data-mime=\"text/x-swift\">Swift (5.2.1)</option>\n" +
                "\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\t<option value=\"4056\" data-mime=\"text/plain\">Text (cat 8.28)</option>\n" +
                "\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\t<option value=\"4057\" data-mime=\"text/typescript\">TypeScript (3.8)</option>\n" +
                "\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\t<option value=\"4058\" data-mime=\"text/x-vb\">Visual Basic (.NET Core 3.1.101)</option>\n" +
                "\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\t<option value=\"4059\" data-mime=\"text/x-sh\">Zsh (5.4.2)</option>\n" +
                "\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\t<option value=\"4060\" data-mime=\"text/x-cobol\">COBOL - Fixed (OpenCOBOL 1.1.0)</option>\n" +
                "\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\t<option value=\"4061\" data-mime=\"text/x-cobol\">COBOL - Free (OpenCOBOL 1.1.0)</option>\n" +
                "\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\t<option value=\"4062\" data-mime=\"text/x-brainfuck\">Brainfuck (bf 20041219)</option>\n" +
                "\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\t<option value=\"4063\" data-mime=\"text/x-ada\">Ada2012 (GNAT 9.2.1)</option>\n" +
                "\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\t<option value=\"4064\" data-mime=\"text/x-unlambda\">Unlambda (2.0.0)</option>\n" +
                "\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\t<option value=\"4065\" data-mime=\"text/x-python\">Cython (0.29.16)</option>\n" +
                "\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\t<option value=\"4066\" data-mime=\"text/x-sh\">Sed (4.4)</option>\n" +
                "\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\t<option value=\"4067\" data-mime=\"text/x-vim\">Vim (8.2.0460)</option>";
        Pattern pattern1 = Pattern.compile("<option value=\"([\\s\\S]*?)\" data-mime=\"[\\s\\S]*?\">[\\s\\S]*?</option>");
        Pattern pattern2 = Pattern.compile("<option value=\"[\\s\\S]*?\" data-mime=\"[\\s\\S]*?\">([\\s\\S]*?)</option>");
        Pattern pattern3 = Pattern.compile("<option value=\"[\\s\\S]*?\" data-mime=\"([\\s\\S]*?)\">[\\s\\S]*?</option>");
        List<String> allGroups1 = ReUtil.findAll(pattern1, body, 1);
        List<String> allGroups2 = ReUtil.findAll(pattern2, body, 1);
        List<String> allGroups3 = ReUtil.findAll(pattern3, body, 1);
        for (int i = 0; i < allGroups1.size(); i++) {
            System.out.println("nameMapContestType.put(\"" + allGroups2.get(i) + "\", \"" + allGroups3.get(i) + "\");");
        }
        for (int i = 0; i < allGroups1.size(); i++) {
            System.out.println("languageMap.put(\"" + allGroups2.get(i) + "\", \"" + allGroups1.get(i) + "\");");
        }
    }
}