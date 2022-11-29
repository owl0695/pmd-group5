/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.symbols.internal.asm;

import static net.sourceforge.pmd.lang.java.symbols.internal.asm.TypeAnnotReflectionTest.assertHasTypeAnnots;
import static net.sourceforge.pmd.util.CollectionUtil.emptyList;
import static net.sourceforge.pmd.util.CollectionUtil.listOf;
import static org.hamcrest.MatcherAssert.assertThat;

import java.lang.annotation.Annotation;
import java.util.List;

import org.hamcrest.Matchers;
import org.junit.Test;

import net.sourceforge.pmd.lang.java.JavaParsingHelper;
import net.sourceforge.pmd.lang.java.symbols.internal.asm.TypeAnnotReflectionTest.AnnotAImpl;
import net.sourceforge.pmd.lang.java.symbols.internal.asm.TypeAnnotReflectionTest.AnnotBImpl;
import net.sourceforge.pmd.lang.java.symbols.testdata.ClassWithTypeAnnotationsOnMethods;
import net.sourceforge.pmd.lang.java.types.JClassType;
import net.sourceforge.pmd.lang.java.types.JIntersectionType;
import net.sourceforge.pmd.lang.java.types.JMethodSig;
import net.sourceforge.pmd.lang.java.types.TypeSystem;

/**
 *
 */
public class TypeAnnotReflectionOnMethodsTest {

    private final TypeSystem ts = JavaParsingHelper.TEST_TYPE_SYSTEM;

    JClassType sym = (JClassType) ts.declaration(ts.getClassSymbol(ClassWithTypeAnnotationsOnMethods.class));

    private final List<Annotation> aAnnot = listOf(new AnnotAImpl());
    private final List<Annotation> bAnnot = listOf(new AnnotBImpl());
    private final List<Annotation> aAndBAnnot = listOf(new AnnotAImpl(), new AnnotBImpl());


    @Test
    public void testTypeAnnotOnParameter() {

        /*
            abstract void aOnIntParam(@A int i);

            abstract void aOnStringParam(@A String i);
         */

        {
            JMethodSig t = getMethodType(sym, "aOnIntParam");
            assertHasTypeAnnots(t.getFormalParameters().get(0), aAnnot);
            assertHasTypeAnnots(t.getReturnType(), emptyList());
        }
        {
            JMethodSig t = getMethodType(sym, "aOnStringParam");
            assertHasTypeAnnots(t.getFormalParameters().get(0), aAnnot);
            assertHasTypeAnnots(t.getReturnType(), emptyList());
        }
    }


    @Test
    public void testTypeAnnotOnReturn() {

        /*
            abstract @A @B String abOnReturn(@A String i);
         */

        {
            JMethodSig t = getMethodType(sym, "abOnReturn");
            assertHasTypeAnnots(t.getFormalParameters().get(0), aAnnot);
            assertHasTypeAnnots(t.getReturnType(), aAndBAnnot);
        }
    }

    @Test
    public void testTypeAnnotOnThrows() {

        /*
            abstract void aOnThrows() throws @A RuntimeException;
         */

        {
            JMethodSig t = getMethodType(sym, "aOnThrows");
            assertHasTypeAnnots(t.getReturnType(), emptyList());
            assertHasTypeAnnots(t.getThrownExceptions().get(0), aAnnot);
        }
    }

    @Test
    public void testTypeAnnotOnTParam() {

        /*

    abstract <@A @B T, E extends T> void abOnTypeParm();
    abstract <@A @B T, E extends T> T abOnTypeParm2(T t);
    abstract <@A T, E extends @B T> void bOnTypeParmBound();
    abstract <@A T, E extends @B T> E bOnTypeParmBound(T t);


         */

        {
            JMethodSig t = getMethodType(sym, "abOnTypeParm");
            assertHasTypeAnnots(t.getTypeParameters().get(0), aAndBAnnot);
            assertHasTypeAnnots(t.getTypeParameters().get(1), emptyList());
        }
        {
            JMethodSig t = getMethodType(sym, "abOnTypeParm2");
            assertHasTypeAnnots(t.getTypeParameters().get(0), aAndBAnnot);
            assertHasTypeAnnots(t.getTypeParameters().get(1), emptyList());
            assertHasTypeAnnots(t.getReturnType(), emptyList());
            assertHasTypeAnnots(t.getFormalParameters().get(0), emptyList());
        }
    }

    @Test
    public void testTypeAnnotOnTParamBound() {

        /*
            abstract <@A T, E extends @B T> void bOnTypeParmBound();
            abstract <@A T, E extends @B T> E bOnTypeParmBound(T t);
            abstract <@A T, E extends @B Cloneable & @A Serializable> E bOnTypeParmBoundIntersection(T t);
         */

        {
            JMethodSig t = getMethodType(sym, "bOnTypeParmBound");
            assertHasTypeAnnots(t.getTypeParameters().get(0), aAnnot);
            assertHasTypeAnnots(t.getTypeParameters().get(1), emptyList());
            assertHasTypeAnnots(t.getTypeParameters().get(1).getUpperBound(), bAnnot);
        }
        {
            JMethodSig t = getMethodType(sym, "bOnTypeParmBound");
            assertHasTypeAnnots(t.getTypeParameters().get(0), aAnnot);
            assertHasTypeAnnots(t.getTypeParameters().get(1), emptyList());
            assertHasTypeAnnots(t.getTypeParameters().get(1).getUpperBound(), bAnnot);

            assertHasTypeAnnots(t.getReturnType(), emptyList());
            assertHasTypeAnnots(t.getFormalParameters().get(0), aAnnot); // this is inherited from the declaration
        }
        {
            JMethodSig t = getMethodType(sym, "bOnTypeParmBoundIntersection");
            assertHasTypeAnnots(t.getTypeParameters().get(0), aAnnot);
            assertHasTypeAnnots(t.getTypeParameters().get(1), emptyList());
            assertHasTypeAnnots(t.getFormalParameters().get(0), aAnnot); // this is inherited from the declaration

            JIntersectionType ub = (JIntersectionType) t.getTypeParameters().get(1).getUpperBound();
            assertHasTypeAnnots(ub, emptyList());
            assertHasTypeAnnots(ub.getComponents().get(0), bAnnot);
            assertHasTypeAnnots(ub.getComponents().get(1), aAnnot);
        }
    }

    @Test
    public void testTypeAnnotOnReceiver() {

        /*
            abstract void abOnReceiver(@A @B ClassWithTypeAnnotationsOnMethods this);
         */

        {
            JMethodSig t = getMethodType(sym, "abOnReceiver");
            assertThat(t.getFormalParameters(), Matchers.empty());
            assertHasTypeAnnots(t.getAnnotatedReceiverType(), aAndBAnnot);
        }
    }

    private static JMethodSig getMethodType(JClassType sym, String fieldName) {
        return sym.streamMethods(it -> it.nameEquals(fieldName)).findFirst().get();
    }

}
