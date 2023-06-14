package ru.bmstu.oberoncompiler.antlr4.impl;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import oberon.antlr4.OberonBaseListener;
import oberon.antlr4.OberonParser;
import org.antlr.v4.runtime.ParserRuleContext;

@Slf4j
@AllArgsConstructor
public class OberonWalker extends OberonBaseListener {
    private OberonParser parser;
    public void enterModule(OberonParser.ModuleContext context) {
        log.info("Entering Module rule : {}", context.getText());
    }

    public void enterEveryRule(ParserRuleContext context) {
        log.info("NAME: {}, RULE: {}", parser.getRuleNames()[context.getRuleIndex()], context.getText());
    }

}
