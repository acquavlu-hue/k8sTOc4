package com.k8stoc4.presenter;

import com.k8stoc4.model.C4Relationship;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class C4RelationshipPresenterTest {

    @Test
    public void testRelationshipWithoutTag() {
        final C4Relationship relationship = new C4Relationship("source", "target", "", "");
        final String expected = "source -> target\n";
        assertEquals(expected, C4RelationshipPresenter.present(relationship));
    }

    @Test
    public void testRelationshipWithTag() {
        final C4Relationship relationship = new C4Relationship("source", "target", "", "", "#tag");
        final String expected = "source -> target {\n    #tag\n}\n";
        assertEquals(expected, C4RelationshipPresenter.present(relationship));
    }
}
