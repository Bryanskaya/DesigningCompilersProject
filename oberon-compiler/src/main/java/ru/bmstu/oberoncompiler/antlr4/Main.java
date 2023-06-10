package ru.bmstu.oberoncompiler.antlr4;

import lombok.extern.slf4j.Slf4j;
import oberon.antlr4.OberonLexer;
import oberon.antlr4.OberonParser;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;



@Slf4j
public class Main {
    public static void main(String[] args) throws Exception {
        String filename = "./src/test/data/test_1.txt";

        try {
            OberonLexer lexer = new OberonLexer(CharStreams.fromFileName(filename));
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            OberonParser parser = new OberonParser(tokens);
            ParseTree tree = parser.module();

            log.info("Tree: {}", tree.toStringTree(parser));

            ParseTreeWalker walker = new ParseTreeWalker();
            walker.walk(new OberonWalker(), tree);
        } catch (Exception e) {
            log.error("Error: " + e.getMessage());
        }

    }
}
