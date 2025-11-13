package visitor;

public interface K8sVisitable {

    void accept(KubernetesResourceVisitor visitor);
}
