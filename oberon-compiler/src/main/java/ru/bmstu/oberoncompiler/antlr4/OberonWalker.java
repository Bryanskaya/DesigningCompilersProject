package ru.bmstu.oberoncompiler.antlr4;

import lombok.extern.slf4j.Slf4j;
import oberon.antlr4.OberonBaseListener;
import oberon.antlr4.OberonParser;
import org.antlr.v4.runtime.ParserRuleContext;

@Slf4j
public class OberonWalker extends OberonBaseListener {
    public void enterModule(OberonParser.ModuleContext context) {
        log.info("Entering Module rule : {}", context.getText());
    }

    public void enterEveryRule(ParserRuleContext context) {
        log.info("Rule: {}, index: {}", context.getText(), context.getRuleIndex());
    }

}
