package org.example;

import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;
import lombok.extern.java.Log;
import lombok.extern.log4j.Log4j;
import render.C4DslRenderer;
import visitor.C4ModelBuilderVisitor;
import visitor.VisitorUtils;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;

@Log
public class KubernetesC4FromYamlVisitor {

    public static void main(String[] args) throws Exception {

        try (KubernetesClient client = new KubernetesClientBuilder().build()) {
            FileInputStream fis=new FileInputStream(new File("src/main/resources/deployment.yaml"));

            List<HasMetadata> resources = client.load(fis).items();

            C4ModelBuilderVisitor visitor = new C4ModelBuilderVisitor();
            for (HasMetadata r : resources) {
                VisitorUtils.accept(r, visitor);
            }

            C4DslRenderer renderer=new C4DslRenderer();
            System.out.println(renderer.renderModel(visitor.getModel(),"prova"));

        }
    }
}
