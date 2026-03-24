package de.gamblegamez.rucksack;

import io.papermc.paper.plugin.loader.PluginClasspathBuilder;
import io.papermc.paper.plugin.loader.PluginLoader;
import io.papermc.paper.plugin.loader.library.impl.MavenLibraryResolver;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.repository.RemoteRepository;

@SuppressWarnings({
        "unused",
        "UnstableApiUsage"
})
public class Loader implements PluginLoader {
    @Override
    public void classloader(PluginClasspathBuilder pluginClasspathBuilder) {
        MavenLibraryResolver resolver = new MavenLibraryResolver();

        resolver.addRepository(new RemoteRepository.Builder("central","default", "https://maven-central.storage-download.googleapis.com/maven2").build());

        // ZSTD
        resolver.addDependency(new Dependency(new DefaultArtifact("com.github.luben:zstd-jni:1.5.7-1"), null));

        // Hibernate + Jakarta Persistence + MySQL
        resolver.addDependency(new Dependency(new DefaultArtifact("org.hibernate.orm:hibernate-core:6.6.15.Final"), null));
        resolver.addDependency(new Dependency(new DefaultArtifact("jakarta.persistence:jakarta.persistence-api:3.1.0"), null));
        resolver.addDependency(new Dependency(new DefaultArtifact("com.mysql:mysql-connector-j:8.4.0"), null));

        pluginClasspathBuilder.addLibrary(resolver);
    }
}
