package ru.bmstu.oberoncompiler.antlr4.impl;

import lombok.Setter;
import org.bytedeco.llvm.LLVM.LLVMTypeRef;
import org.bytedeco.llvm.LLVM.LLVMValueRef;


@Setter
public class ValueRef extends LLVMValueRef {
    public LLVMTypeRef elemType;

    ValueRef(LLVMValueRef valRef, LLVMTypeRef typeRef) {
        super(valRef);
        elemType = typeRef;
    }
}
