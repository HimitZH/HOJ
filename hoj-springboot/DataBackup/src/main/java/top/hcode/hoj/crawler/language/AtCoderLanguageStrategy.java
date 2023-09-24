package top.hcode.hoj.crawler.language;

import cn.hutool.core.util.ReUtil;
import cn.hutool.http.HtmlUtil;
import top.hcode.hoj.pojo.entity.problem.Language;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    private static Map<String, String> aceMapCodeMirrorMap = new HashMap<>();

    static {
        aceMapCodeMirrorMap.put("c_cpp", "text/x-c++src");
        aceMapCodeMirrorMap.put("golang", "text/golang");
        aceMapCodeMirrorMap.put("csharp", "text/x-csharp");
        aceMapCodeMirrorMap.put("kotlin", "text/x-kotlin");
        aceMapCodeMirrorMap.put("java", "text/x-java");
        aceMapCodeMirrorMap.put("nim", "text/x-nim");
        aceMapCodeMirrorMap.put("text", "text/plain");
        aceMapCodeMirrorMap.put("javascript", "text/javascript");
        aceMapCodeMirrorMap.put("r", "text/x-r");
        aceMapCodeMirrorMap.put("d", "text/x-d");
        aceMapCodeMirrorMap.put("swift", "text/x-swift");
        aceMapCodeMirrorMap.put("dart", "application/dart");
        aceMapCodeMirrorMap.put("php", "text/x-php");
        aceMapCodeMirrorMap.put("ruby", "text/x-ruby");
        aceMapCodeMirrorMap.put("crystal", "text/x-crystal");
        aceMapCodeMirrorMap.put("fsharp", "text/x-fsharp");
        aceMapCodeMirrorMap.put("julia", "text/x-julia");
        aceMapCodeMirrorMap.put("sh", "text/x-sh");
        aceMapCodeMirrorMap.put("fortran", "text/x-fortran");
        aceMapCodeMirrorMap.put("haskell", "text/x-haskell");
        aceMapCodeMirrorMap.put("lua", "text/x-lua");
        aceMapCodeMirrorMap.put("lisp", "text/x-lisp");
        aceMapCodeMirrorMap.put("cobol", "text/x-cobol");
        aceMapCodeMirrorMap.put("python", "text/x-python");
        aceMapCodeMirrorMap.put("perl", "text/x-perl");
        aceMapCodeMirrorMap.put("pascal", "text/x-pascal");
        aceMapCodeMirrorMap.put("scala", "text/x-scala");
        aceMapCodeMirrorMap.put("prolog", "text/x-prolog");
        aceMapCodeMirrorMap.put("vbscript", "text/x-vbscript");
        aceMapCodeMirrorMap.put("scheme", "text/x-scheme");
        aceMapCodeMirrorMap.put("clojure", "text/x-clojure");
        aceMapCodeMirrorMap.put("erlang", "text/x-erlang");
        aceMapCodeMirrorMap.put("typescript", "text/typescript");
        aceMapCodeMirrorMap.put("rust", "text/x-rustsrc");
        aceMapCodeMirrorMap.put("ocaml", "text/x-ocaml");
        aceMapCodeMirrorMap.put("raku", "text/x-raku");
        aceMapCodeMirrorMap.put("ada", "text/x-ada");
        aceMapCodeMirrorMap.put("matlab", "text/x-matlab");
        aceMapCodeMirrorMap.put("haxe", "text/x-haxe");
        aceMapCodeMirrorMap.put("elixir", "text/x-elixir");
    }

    @Override
    public Collection<String> getLangList() {
        HashMap<String, String> languageMap = new HashMap<>();
        languageMap.put("C++ 20 (gcc 12.2)", "5001");
        languageMap.put("Go (go 1.20.6)", "5002");
        languageMap.put("C# 11.0 (.NET 7.0.7)", "5003");
        languageMap.put("Kotlin (Kotlin/JVM 1.8.20)", "5004");
        languageMap.put("Java (OpenJDK 17)", "5005");
        languageMap.put("Nim (Nim 1.6.14)", "5006");
        languageMap.put("V (V 0.4)", "5007");
        languageMap.put("Zig (Zig 0.10.1)", "5008");
        languageMap.put("JavaScript (Node.js 18.16.1)", "5009");
        languageMap.put("JavaScript (Deno 1.35.1)", "5010");
        languageMap.put("R (GNU R 4.2.1)", "5011");
        languageMap.put("D (DMD 2.104.0)", "5012");
        languageMap.put("D (LDC 1.32.2)", "5013");
        languageMap.put("Swift (swift 5.8.1)", "5014");
        languageMap.put("Dart (Dart 3.0.5)", "5015");
        languageMap.put("PHP (php 8.2.8)", "5016");
        languageMap.put("C (gcc 12.2.0)", "5017");
        languageMap.put("Ruby (ruby 3.2.2)", "5018");
        languageMap.put("Crystal (Crystal 1.9.1)", "5019");
        languageMap.put("Brainfuck (bf 20041219)", "5020");
        languageMap.put("F# 7.0 (.NET 7.0.7)", "5021");
        languageMap.put("Julia (Julia 1.9.2)", "5022");
        languageMap.put("Bash (bash 5.2.2)", "5023");
        languageMap.put("Text (cat 8.32)", "5024");
        languageMap.put("Haskell (GHC 9.4.5)", "5025");
        languageMap.put("Fortran (gfortran 12.2)", "5026");
        languageMap.put("Lua (LuaJIT 2.1.0-beta3)", "5027");
        languageMap.put("C++ 23 (gcc 12.2)", "5028");
        languageMap.put("Common Lisp (SBCL 2.3.6)", "5029");
        languageMap.put("COBOL (Free) (GnuCOBOL 3.1.2)", "5030");
        languageMap.put("C++ 23 (Clang 16.0.6)", "5031");
        languageMap.put("Zsh (Zsh 5.9)", "5032");
        languageMap.put("SageMath (SageMath 9.5)", "5033");
        languageMap.put("Sed (GNU sed 4.8)", "5034");
        languageMap.put("bc (bc 1.07.1)", "5035");
        languageMap.put("dc (dc 1.07.1)", "5036");
        languageMap.put("Perl (perl  5.34)", "5037");
        languageMap.put("AWK (GNU Awk 5.0.1)", "5038");
        languageMap.put("なでしこ (cnako3 3.4.20)", "5039");
        languageMap.put("Assembly x64 (NASM 2.15.05)", "5040");
        languageMap.put("Pascal (fpc 3.2.2)", "5041");
        languageMap.put("C# 11.0 AOT (.NET 7.0.7)", "5042");
        languageMap.put("Lua (Lua 5.4.6)", "5043");
        languageMap.put("Prolog (SWI-Prolog 9.0.4)", "5044");
        languageMap.put("PowerShell (PowerShell 7.3.1)", "5045");
        languageMap.put("Scheme (Gauche 0.9.12)", "5046");
        languageMap.put("Scala 3.3.0 (Scala Native 0.4.14)", "5047");
        languageMap.put("Visual Basic 16.9 (.NET 7.0.7)", "5048");
        languageMap.put("Forth (gforth 0.7.3)", "5049");
        languageMap.put("Clojure (babashka 1.3.181)", "5050");
        languageMap.put("Erlang (Erlang 26.0.2)", "5051");
        languageMap.put("TypeScript 5.1 (Deno 1.35.1)", "5052");
        languageMap.put("C++ 17 (gcc 12.2)", "5053");
        languageMap.put("Rust (rustc 1.70.0)", "5054");
        languageMap.put("Python (CPython 3.11.4)", "5055");
        languageMap.put("Scala (Dotty 3.3.0)", "5056");
        languageMap.put("Koka (koka 2.4.0)", "5057");
        languageMap.put("TypeScript 5.1 (Node.js 18.16.1)", "5058");
        languageMap.put("OCaml (ocamlopt 5.0.0)", "5059");
        languageMap.put("Raku (Rakudo 2023.06)", "5060");
        languageMap.put("Vim (vim 9.0.0242)", "5061");
        languageMap.put("Emacs Lisp (Native Compile) (GNU Emacs 28.2)", "5062");
        languageMap.put("Python (Mambaforge / CPython 3.10.10)", "5063");
        languageMap.put("Clojure (clojure 1.11.1)", "5064");
        languageMap.put("プロデル (mono版プロデル 1.9.1182)", "5065");
        languageMap.put("ECLiPSe (ECLiPSe 7.1_13)", "5066");
        languageMap.put("Nibbles (literate form) (nibbles 1.01)", "5067");
        languageMap.put("Ada (GNAT 12.2)", "5068");
        languageMap.put("jq (jq 1.6)", "5069");
        languageMap.put("Cyber (Cyber v0.2-Latest)", "5070");
        languageMap.put("Carp (Carp 0.5.5)", "5071");
        languageMap.put("C++ 17 (Clang 16.0.6)", "5072");
        languageMap.put("C++ 20 (Clang 16.0.6)", "5073");
        languageMap.put("LLVM IR (Clang 16.0.6)", "5074");
        languageMap.put("Emacs Lisp (Byte Compile) (GNU Emacs 28.2)", "5075");
        languageMap.put("Factor (Factor 0.98)", "5076");
        languageMap.put("D (GDC 12.2)", "5077");
        languageMap.put("Python (PyPy 3.10-v7.3.12)", "5078");
        languageMap.put("Whitespace (whitespacers 1.0.0)", "5079");
        languageMap.put("><> (fishr 0.1.0)", "5080");
        languageMap.put("ReasonML (reason 3.9.0)", "5081");
        languageMap.put("Python (Cython 0.29.34)", "5082");
        languageMap.put("Octave (GNU Octave 8.2.0)", "5083");
        languageMap.put("Haxe (JVM) (Haxe 4.3.1)", "5084");
        languageMap.put("Elixir (Elixir 1.15.2)", "5085");
        languageMap.put("Mercury (Mercury 22.01.6)", "5086");
        languageMap.put("Seed7 (Seed7 3.2.1)", "5087");
        languageMap.put("Emacs Lisp (No Compile) (GNU Emacs 28.2)", "5088");
        languageMap.put("Unison (Unison M5b)", "5089");
        languageMap.put("COBOL (GnuCOBOL(Fixed) 3.1.2)", "5090");
        return languageMap.keySet();
    }

    @Override
    public String getOJName() {
        return "AC";
    }

    @Override
    protected String getLangContentType(String name) {
        HashMap<String, String> nameMapContestType = new HashMap<>();
        nameMapContestType.put("C++ 20 (gcc 12.2)", "text/x-c++src");
        nameMapContestType.put("Go (go 1.20.6)", "text/golang");
        nameMapContestType.put("C# 11.0 (.NET 7.0.7)", "text/x-csharp");
        nameMapContestType.put("Kotlin (Kotlin/JVM 1.8.20)", "text/x-kotlin");
        nameMapContestType.put("Java (OpenJDK 17)", "text/x-java");
        nameMapContestType.put("Nim (Nim 1.6.14)", "text/x-nim");
        nameMapContestType.put("V (V 0.4)", "text/plain");
        nameMapContestType.put("Zig (Zig 0.10.1)", "text/plain");
        nameMapContestType.put("JavaScript (Node.js 18.16.1)", "text/javascript");
        nameMapContestType.put("JavaScript (Deno 1.35.1)", "text/javascript");
        nameMapContestType.put("R (GNU R 4.2.1)", "text/x-r");
        nameMapContestType.put("D (DMD 2.104.0)", "text/x-d");
        nameMapContestType.put("D (LDC 1.32.2)", "text/x-d");
        nameMapContestType.put("Swift (swift 5.8.1)", "text/x-swift");
        nameMapContestType.put("Dart (Dart 3.0.5)", "application/dart");
        nameMapContestType.put("PHP (php 8.2.8)", "text/x-php");
        nameMapContestType.put("C (gcc 12.2.0)", "text/x-c++src");
        nameMapContestType.put("Ruby (ruby 3.2.2)", "text/x-ruby");
        nameMapContestType.put("Crystal (Crystal 1.9.1)", "text/x-crystal");
        nameMapContestType.put("Brainfuck (bf 20041219)", "text/plain");
        nameMapContestType.put("F# 7.0 (.NET 7.0.7)", "text/x-fsharp");
        nameMapContestType.put("Julia (Julia 1.9.2)", "text/x-julia");
        nameMapContestType.put("Bash (bash 5.2.2)", "text/x-sh");
        nameMapContestType.put("Text (cat 8.32)", "text/plain");
        nameMapContestType.put("Haskell (GHC 9.4.5)", "text/x-haskell");
        nameMapContestType.put("Fortran (gfortran 12.2)", "text/x-fortran");
        nameMapContestType.put("Lua (LuaJIT 2.1.0-beta3)", "text/x-lua");
        nameMapContestType.put("C++ 23 (gcc 12.2)", "text/x-c++src");
        nameMapContestType.put("Common Lisp (SBCL 2.3.6)", "text/x-lisp");
        nameMapContestType.put("COBOL (Free) (GnuCOBOL 3.1.2)", "text/x-cobol");
        nameMapContestType.put("C++ 23 (Clang 16.0.6)", "text/x-c++src");
        nameMapContestType.put("Zsh (Zsh 5.9)", "text/x-sh");
        nameMapContestType.put("SageMath (SageMath 9.5)", "text/x-python");
        nameMapContestType.put("Sed (GNU sed 4.8)", "text/x-sh");
        nameMapContestType.put("bc (bc 1.07.1)", "text/plain");
        nameMapContestType.put("dc (dc 1.07.1)", "text/plain");
        nameMapContestType.put("Perl (perl  5.34)", "text/x-perl");
        nameMapContestType.put("AWK (GNU Awk 5.0.1)", "text/x-sh");
        nameMapContestType.put("なでしこ (cnako3 3.4.20)", "text/plain");
        nameMapContestType.put("Assembly x64 (NASM 2.15.05)", "text/plain");
        nameMapContestType.put("Pascal (fpc 3.2.2)", "text/x-pascal");
        nameMapContestType.put("C# 11.0 AOT (.NET 7.0.7)", "text/x-csharp");
        nameMapContestType.put("Lua (Lua 5.4.6)", "text/x-lua");
        nameMapContestType.put("Prolog (SWI-Prolog 9.0.4)", "text/x-prolog");
        nameMapContestType.put("PowerShell (PowerShell 7.3.1)", "text/x-sh");
        nameMapContestType.put("Scheme (Gauche 0.9.12)", "text/x-scheme");
        nameMapContestType.put("Scala 3.3.0 (Scala Native 0.4.14)", "text/x-scala");
        nameMapContestType.put("Visual Basic 16.9 (.NET 7.0.7)", "text/x-vbscript");
        nameMapContestType.put("Forth (gforth 0.7.3)", "text/plain");
        nameMapContestType.put("Clojure (babashka 1.3.181)", "text/x-clojure");
        nameMapContestType.put("Erlang (Erlang 26.0.2)", "text/x-erlang");
        nameMapContestType.put("TypeScript 5.1 (Deno 1.35.1)", "text/typescript");
        nameMapContestType.put("C++ 17 (gcc 12.2)", "text/x-c++src");
        nameMapContestType.put("Rust (rustc 1.70.0)", "text/x-rustsrc");
        nameMapContestType.put("Python (CPython 3.11.4)", "text/x-python");
        nameMapContestType.put("Scala (Dotty 3.3.0)", "text/x-scala");
        nameMapContestType.put("Koka (koka 2.4.0)", "text/plain");
        nameMapContestType.put("TypeScript 5.1 (Node.js 18.16.1)", "text/typescript");
        nameMapContestType.put("OCaml (ocamlopt 5.0.0)", "text/x-ocaml");
        nameMapContestType.put("Raku (Rakudo 2023.06)", "text/x-raku");
        nameMapContestType.put("Vim (vim 9.0.0242)", "text/plain");
        nameMapContestType.put("Emacs Lisp (Native Compile) (GNU Emacs 28.2)", "text/x-lisp");
        nameMapContestType.put("Python (Mambaforge / CPython 3.10.10)", "text/x-python");
        nameMapContestType.put("Clojure (clojure 1.11.1)", "text/x-clojure");
        nameMapContestType.put("プロデル (mono版プロデル 1.9.1182)", "text/plain");
        nameMapContestType.put("ECLiPSe (ECLiPSe 7.1_13)", "text/plain");
        nameMapContestType.put("Nibbles (literate form) (nibbles 1.01)", "text/plain");
        nameMapContestType.put("Ada (GNAT 12.2)", "text/x-ada");
        nameMapContestType.put("jq (jq 1.6)", "text/plain");
        nameMapContestType.put("Cyber (Cyber v0.2-Latest)", "text/plain");
        nameMapContestType.put("Carp (Carp 0.5.5)", "text/x-clojure");
        nameMapContestType.put("C++ 17 (Clang 16.0.6)", "text/x-c++src");
        nameMapContestType.put("C++ 20 (Clang 16.0.6)", "text/x-c++src");
        nameMapContestType.put("LLVM IR (Clang 16.0.6)", "text/plain");
        nameMapContestType.put("Emacs Lisp (Byte Compile) (GNU Emacs 28.2)", "text/x-lisp");
        nameMapContestType.put("Factor (Factor 0.98)", "text/plain");
        nameMapContestType.put("D (GDC 12.2)", "text/x-d");
        nameMapContestType.put("Python (PyPy 3.10-v7.3.12)", "text/x-python");
        nameMapContestType.put("Whitespace (whitespacers 1.0.0)", "text/plain");
        nameMapContestType.put("><> (fishr 0.1.0)", "text/plain");
        nameMapContestType.put("ReasonML (reason 3.9.0)", "text/x-ocaml");
        nameMapContestType.put("Python (Cython 0.29.34)", "text/x-python");
        nameMapContestType.put("Octave (GNU Octave 8.2.0)", "text/x-matlab");
        nameMapContestType.put("Haxe (JVM) (Haxe 4.3.1)", "text/x-haxe");
        nameMapContestType.put("Elixir (Elixir 1.15.2)", "text/x-elixir");
        nameMapContestType.put("Mercury (Mercury 22.01.6)", "text/plain");
        nameMapContestType.put("Seed7 (Seed7 3.2.1)", "text/plain");
        nameMapContestType.put("Emacs Lisp (No Compile) (GNU Emacs 28.2)", "text/x-lisp");
        nameMapContestType.put("Unison (Unison M5b)", "text/plain");
        nameMapContestType.put("COBOL (GnuCOBOL(Fixed) 3.1.2)", "text/x-cobol");
        return nameMapContestType.get(name);
    }

    public static void main(String[] args) {
        String body = "\n" +
                "\t\t\t\t\t\t\t\t\t<option value=\"5001\" data-ace-mode=\"c_cpp\">C&#43;&#43; 20 (gcc 12.2)</option>\n" +
                "\t\t\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\t\t\t<option value=\"5002\" data-ace-mode=\"golang\">Go (go 1.20.6)</option>\n" +
                "\t\t\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\t\t\t<option value=\"5003\" data-ace-mode=\"csharp\">C# 11.0 (.NET 7.0.7)</option>\n" +
                "\t\t\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\t\t\t<option value=\"5004\" data-ace-mode=\"kotlin\">Kotlin (Kotlin/JVM 1.8.20)</option>\n" +
                "\t\t\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\t\t\t<option value=\"5005\" data-ace-mode=\"java\">Java (OpenJDK 17)</option>\n" +
                "\t\t\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\t\t\t<option value=\"5006\" data-ace-mode=\"nim\">Nim (Nim 1.6.14)</option>\n" +
                "\t\t\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\t\t\t<option value=\"5007\" data-ace-mode=\"text\">V (V 0.4)</option>\n" +
                "\t\t\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\t\t\t<option value=\"5008\" data-ace-mode=\"text\">Zig (Zig 0.10.1)</option>\n" +
                "\t\t\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\t\t\t<option value=\"5009\" data-ace-mode=\"javascript\">JavaScript (Node.js 18.16.1)</option>\n" +
                "\t\t\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\t\t\t<option value=\"5010\" data-ace-mode=\"javascript\">JavaScript (Deno 1.35.1)</option>\n" +
                "\t\t\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\t\t\t<option value=\"5011\" data-ace-mode=\"r\">R (GNU R 4.2.1)</option>\n" +
                "\t\t\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\t\t\t<option value=\"5012\" data-ace-mode=\"d\">D (DMD 2.104.0)</option>\n" +
                "\t\t\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\t\t\t<option value=\"5013\" data-ace-mode=\"d\">D (LDC 1.32.2)</option>\n" +
                "\t\t\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\t\t\t<option value=\"5014\" data-ace-mode=\"swift\">Swift (swift 5.8.1)</option>\n" +
                "\t\t\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\t\t\t<option value=\"5015\" data-ace-mode=\"dart\">Dart (Dart 3.0.5)</option>\n" +
                "\t\t\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\t\t\t<option value=\"5016\" data-ace-mode=\"php\">PHP (php 8.2.8)</option>\n" +
                "\t\t\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\t\t\t<option value=\"5017\" data-ace-mode=\"c_cpp\">C (gcc 12.2.0)</option>\n" +
                "\t\t\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\t\t\t<option value=\"5018\" data-ace-mode=\"ruby\">Ruby (ruby 3.2.2)</option>\n" +
                "\t\t\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\t\t\t<option value=\"5019\" data-ace-mode=\"crystal\">Crystal (Crystal 1.9.1)</option>\n" +
                "\t\t\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\t\t\t<option value=\"5020\" data-ace-mode=\"text\">Brainfuck (bf 20041219)</option>\n" +
                "\t\t\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\t\t\t<option value=\"5021\" data-ace-mode=\"fsharp\">F# 7.0 (.NET 7.0.7)</option>\n" +
                "\t\t\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\t\t\t<option value=\"5022\" data-ace-mode=\"julia\">Julia (Julia 1.9.2)</option>\n" +
                "\t\t\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\t\t\t<option value=\"5023\" data-ace-mode=\"sh\">Bash (bash 5.2.2)</option>\n" +
                "\t\t\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\t\t\t<option value=\"5024\" data-ace-mode=\"text\">Text (cat 8.32)</option>\n" +
                "\t\t\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\t\t\t<option value=\"5025\" data-ace-mode=\"haskell\">Haskell (GHC 9.4.5)</option>\n" +
                "\t\t\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\t\t\t<option value=\"5026\" data-ace-mode=\"fortran\">Fortran (gfortran 12.2)</option>\n" +
                "\t\t\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\t\t\t<option value=\"5027\" data-ace-mode=\"lua\">Lua (LuaJIT 2.1.0-beta3)</option>\n" +
                "\t\t\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\t\t\t<option value=\"5028\" data-ace-mode=\"c_cpp\">C&#43;&#43; 23 (gcc 12.2)</option>\n" +
                "\t\t\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\t\t\t<option value=\"5029\" data-ace-mode=\"lisp\">Common Lisp (SBCL 2.3.6)</option>\n" +
                "\t\t\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\t\t\t<option value=\"5030\" data-ace-mode=\"cobol\">COBOL (Free) (GnuCOBOL 3.1.2)</option>\n" +
                "\t\t\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\t\t\t<option value=\"5031\" data-ace-mode=\"c_cpp\">C&#43;&#43; 23 (Clang 16.0.6)</option>\n" +
                "\t\t\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\t\t\t<option value=\"5032\" data-ace-mode=\"sh\">Zsh (Zsh 5.9)</option>\n" +
                "\t\t\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\t\t\t<option value=\"5033\" data-ace-mode=\"python\">SageMath (SageMath 9.5)</option>\n" +
                "\t\t\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\t\t\t<option value=\"5034\" data-ace-mode=\"sh\">Sed (GNU sed 4.8)</option>\n" +
                "\t\t\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\t\t\t<option value=\"5035\" data-ace-mode=\"text\">bc (bc 1.07.1)</option>\n" +
                "\t\t\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\t\t\t<option value=\"5036\" data-ace-mode=\"text\">dc (dc 1.07.1)</option>\n" +
                "\t\t\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\t\t\t<option value=\"5037\" data-ace-mode=\"perl\">Perl (perl  5.34)</option>\n" +
                "\t\t\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\t\t\t<option value=\"5038\" data-ace-mode=\"sh\">AWK (GNU Awk 5.0.1)</option>\n" +
                "\t\t\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\t\t\t<option value=\"5039\" data-ace-mode=\"text\">なでしこ (cnako3 3.4.20)</option>\n" +
                "\t\t\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\t\t\t<option value=\"5040\" data-ace-mode=\"text\">Assembly x64 (NASM 2.15.05)</option>\n" +
                "\t\t\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\t\t\t<option value=\"5041\" data-ace-mode=\"pascal\">Pascal (fpc 3.2.2)</option>\n" +
                "\t\t\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\t\t\t<option value=\"5042\" data-ace-mode=\"csharp\">C# 11.0 AOT (.NET 7.0.7)</option>\n" +
                "\t\t\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\t\t\t<option value=\"5043\" data-ace-mode=\"lua\">Lua (Lua 5.4.6)</option>\n" +
                "\t\t\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\t\t\t<option value=\"5044\" data-ace-mode=\"prolog\">Prolog (SWI-Prolog 9.0.4)</option>\n" +
                "\t\t\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\t\t\t<option value=\"5045\" data-ace-mode=\"sh\">PowerShell (PowerShell 7.3.1)</option>\n" +
                "\t\t\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\t\t\t<option value=\"5046\" data-ace-mode=\"scheme\">Scheme (Gauche 0.9.12)</option>\n" +
                "\t\t\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\t\t\t<option value=\"5047\" data-ace-mode=\"scala\">Scala 3.3.0 (Scala Native 0.4.14)</option>\n" +
                "\t\t\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\t\t\t<option value=\"5048\" data-ace-mode=\"vbscript\">Visual Basic 16.9 (.NET 7.0.7)</option>\n" +
                "\t\t\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\t\t\t<option value=\"5049\" data-ace-mode=\"text\">Forth (gforth 0.7.3)</option>\n" +
                "\t\t\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\t\t\t<option value=\"5050\" data-ace-mode=\"clojure\">Clojure (babashka 1.3.181)</option>\n" +
                "\t\t\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\t\t\t<option value=\"5051\" data-ace-mode=\"erlang\">Erlang (Erlang 26.0.2)</option>\n" +
                "\t\t\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\t\t\t<option value=\"5052\" data-ace-mode=\"typescript\">TypeScript 5.1 (Deno 1.35.1)</option>\n" +
                "\t\t\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\t\t\t<option value=\"5053\" data-ace-mode=\"c_cpp\">C&#43;&#43; 17 (gcc 12.2)</option>\n" +
                "\t\t\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\t\t\t<option value=\"5054\" data-ace-mode=\"rust\">Rust (rustc 1.70.0)</option>\n" +
                "\t\t\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\t\t\t<option value=\"5055\" data-ace-mode=\"python\">Python (CPython 3.11.4)</option>\n" +
                "\t\t\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\t\t\t<option value=\"5056\" data-ace-mode=\"scala\">Scala (Dotty 3.3.0)</option>\n" +
                "\t\t\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\t\t\t<option value=\"5057\" data-ace-mode=\"text\">Koka (koka 2.4.0)</option>\n" +
                "\t\t\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\t\t\t<option value=\"5058\" data-ace-mode=\"typescript\">TypeScript 5.1 (Node.js 18.16.1)</option>\n" +
                "\t\t\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\t\t\t<option value=\"5059\" data-ace-mode=\"ocaml\">OCaml (ocamlopt 5.0.0)</option>\n" +
                "\t\t\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\t\t\t<option value=\"5060\" data-ace-mode=\"raku\">Raku (Rakudo 2023.06)</option>\n" +
                "\t\t\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\t\t\t<option value=\"5061\" data-ace-mode=\"text\">Vim (vim 9.0.0242)</option>\n" +
                "\t\t\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\t\t\t<option value=\"5062\" data-ace-mode=\"lisp\">Emacs Lisp (Native Compile) (GNU Emacs 28.2)</option>\n" +
                "\t\t\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\t\t\t<option value=\"5063\" data-ace-mode=\"python\">Python (Mambaforge / CPython 3.10.10)</option>\n" +
                "\t\t\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\t\t\t<option value=\"5064\" data-ace-mode=\"clojure\">Clojure (clojure 1.11.1)</option>\n" +
                "\t\t\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\t\t\t<option value=\"5065\" data-ace-mode=\"text\">プロデル (mono版プロデル 1.9.1182)</option>\n" +
                "\t\t\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\t\t\t<option value=\"5066\" data-ace-mode=\"text\">ECLiPSe (ECLiPSe 7.1_13)</option>\n" +
                "\t\t\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\t\t\t<option value=\"5067\" data-ace-mode=\"text\">Nibbles (literate form) (nibbles 1.01)</option>\n" +
                "\t\t\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\t\t\t<option value=\"5068\" data-ace-mode=\"ada\">Ada (GNAT 12.2)</option>\n" +
                "\t\t\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\t\t\t<option value=\"5069\" data-ace-mode=\"text\">jq (jq 1.6)</option>\n" +
                "\t\t\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\t\t\t<option value=\"5070\" data-ace-mode=\"text\">Cyber (Cyber v0.2-Latest)</option>\n" +
                "\t\t\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\t\t\t<option value=\"5071\" data-ace-mode=\"clojure\">Carp (Carp 0.5.5)</option>\n" +
                "\t\t\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\t\t\t<option value=\"5072\" data-ace-mode=\"c_cpp\">C&#43;&#43; 17 (Clang 16.0.6)</option>\n" +
                "\t\t\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\t\t\t<option value=\"5073\" data-ace-mode=\"c_cpp\">C&#43;&#43; 20 (Clang 16.0.6)</option>\n" +
                "\t\t\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\t\t\t<option value=\"5074\" data-ace-mode=\"text\">LLVM IR (Clang 16.0.6)</option>\n" +
                "\t\t\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\t\t\t<option value=\"5075\" data-ace-mode=\"lisp\">Emacs Lisp (Byte Compile) (GNU Emacs 28.2)</option>\n" +
                "\t\t\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\t\t\t<option value=\"5076\" data-ace-mode=\"text\">Factor (Factor 0.98)</option>\n" +
                "\t\t\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\t\t\t<option value=\"5077\" data-ace-mode=\"d\">D (GDC 12.2)</option>\n" +
                "\t\t\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\t\t\t<option value=\"5078\" data-ace-mode=\"python\">Python (PyPy 3.10-v7.3.12)</option>\n" +
                "\t\t\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\t\t\t<option value=\"5079\" data-ace-mode=\"text\">Whitespace (whitespacers 1.0.0)</option>\n" +
                "\t\t\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\t\t\t<option value=\"5080\" data-ace-mode=\"text\">&gt;&lt;&gt; (fishr 0.1.0)</option>\n" +
                "\t\t\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\t\t\t<option value=\"5081\" data-ace-mode=\"ocaml\">ReasonML (reason 3.9.0)</option>\n" +
                "\t\t\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\t\t\t<option value=\"5082\" data-ace-mode=\"python\">Python (Cython 0.29.34)</option>\n" +
                "\t\t\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\t\t\t<option value=\"5083\" data-ace-mode=\"matlab\">Octave (GNU Octave 8.2.0)</option>\n" +
                "\t\t\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\t\t\t<option value=\"5084\" data-ace-mode=\"haxe\">Haxe (JVM) (Haxe 4.3.1)</option>\n" +
                "\t\t\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\t\t\t<option value=\"5085\" data-ace-mode=\"elixir\">Elixir (Elixir 1.15.2)</option>\n" +
                "\t\t\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\t\t\t<option value=\"5086\" data-ace-mode=\"text\">Mercury (Mercury 22.01.6)</option>\n" +
                "\t\t\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\t\t\t<option value=\"5087\" data-ace-mode=\"text\">Seed7 (Seed7 3.2.1)</option>\n" +
                "\t\t\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\t\t\t<option value=\"5088\" data-ace-mode=\"lisp\">Emacs Lisp (No Compile) (GNU Emacs 28.2)</option>\n" +
                "\t\t\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\t\t\t<option value=\"5089\" data-ace-mode=\"text\">Unison (Unison M5b)</option>\n" +
                "\t\t\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\t\t\t<option value=\"5090\" data-ace-mode=\"cobol\">COBOL (GnuCOBOL(Fixed) 3.1.2)</option>\n" +
                "\t\t\t\t\t\t\t\t";
        Pattern pattern1 = Pattern.compile("<option value=\"([\\s\\S]*?)\" data-ace-mode=\"[\\s\\S]*?\">[\\s\\S]*?</option>");
        Pattern pattern2 = Pattern.compile("<option value=\"[\\s\\S]*?\" data-ace-mode=\"[\\s\\S]*?\">([\\s\\S]*?)</option>");
        Pattern pattern3 = Pattern.compile("<option value=\"[\\s\\S]*?\" data-ace-mode=\"([\\s\\S]*?)\">[\\s\\S]*?</option>");
        List<String> allGroups1 = ReUtil.findAll(pattern1, body, 1);
        List<String> allGroups2 = ReUtil.findAll(pattern2, body, 1);
        List<String> allGroups3 = ReUtil.findAll(pattern3, body, 1);
        for (int i = 0; i < allGroups1.size(); i++) {
            String contentType = aceMapCodeMirrorMap.get(allGroups3.get(i));
            if (contentType == null) {
                contentType = "text/x-" + allGroups3.get(i);
            }
            System.out.println("nameMapContestType.put(\"" + HtmlUtil.unescape(allGroups2.get(i)) + "\", \"" + contentType + "\");");
        }
        for (int i = 0; i < allGroups1.size(); i++) {
            System.out.println("languageMap.put(\"" + HtmlUtil.unescape(allGroups2.get(i)) + "\", \"" + allGroups1.get(i) + "\");");
        }
    }
}