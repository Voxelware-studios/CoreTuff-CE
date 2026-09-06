package org.voxelware.coretuff;

import io.papermc.paper.plugin.loader.PluginClasspathBuilder;
import io.papermc.paper.plugin.loader.PluginLoader;
import io.papermc.paper.plugin.loader.library.impl.MavenLibraryResolver;
import org.eclipse.aether.graph.Dependency;

public class dependencyresolver implements PluginLoader {
    @Override
    public void classloader(PluginClasspathBuilder classpathBuilder) {
        MavenLibraryResolver resolver = new MavenLibraryResolver();
        resolver.addRepository(new org.eclipse.aether.repository.RemoteRepository.Builder(
                "central",
                "default",
                MavenLibraryResolver.MAVEN_CENTRAL_DEFAULT_MIRROR
        ).build());
        Dependency h2Dep = new Dependency(
                new org.eclipse.aether.artifact.DefaultArtifact("com.h2database", "h2", "jar", "2.4.240"),
                null
        );
        resolver.addDependency(h2Dep);

        Dependency spongeDep = new Dependency(
                new org.eclipse.aether.artifact.DefaultArtifact("org.spongepowered", "configurate-core", "jar", "4.1.2"),
                null
        );
        resolver.addDependency(spongeDep);

        Dependency lombokDep = new Dependency(
                new org.eclipse.aether.artifact.DefaultArtifact("org.projectlombok", "lombok", "jar", "1.18.46"),
                null
        );
        resolver.addDependency(lombokDep);

        classpathBuilder.addLibrary(resolver);
    }
}
