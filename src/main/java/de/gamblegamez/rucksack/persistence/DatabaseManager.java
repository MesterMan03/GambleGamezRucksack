package de.gamblegamez.rucksack.persistence;

import de.gamblegamez.rucksack.Rucksack;
import org.bukkit.configuration.file.FileConfiguration;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.AvailableSettings;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

public class DatabaseManager implements AutoCloseable {
    private final Rucksack plugin;
    private final SessionFactory sessionFactory;
    private final StandardServiceRegistry serviceRegistry;

    public DatabaseManager(Rucksack plugin) {
        this.plugin = plugin;
        var configuration = plugin.getConfig();

        this.serviceRegistry = buildRegistry(configuration);
        try {
            var currentThread = Thread.currentThread();
            var previousClassLoader = currentThread.getContextClassLoader();

            // Hibernate can run under a parent classloader in Paper; point it at the plugin classloader.
            var pluginClassLoader = plugin.getClass().getClassLoader();
            currentThread.setContextClassLoader(pluginClassLoader);
            try {
                this.sessionFactory = new MetadataSources(serviceRegistry)
                        .addAnnotatedClass(BackpackPageEntity.class)
                        .buildMetadata()
                        .buildSessionFactory();
            } finally {
                currentThread.setContextClassLoader(previousClassLoader);
            }
            runInitScript();
        } catch (Exception exception) {
            StandardServiceRegistryBuilder.destroy(serviceRegistry);
            throw exception;
        }
    }

    private void runInitScript() {
        var initSql = readInitScriptResource();

        Transaction transaction = null;
        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();
            session.doWork(connection -> {
                try (var statement = connection.createStatement()) {
                    statement.execute(initSql);
                }
            });
            transaction.commit();
        } catch (Exception exception) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new IllegalStateException("Failed to run SQL init script", exception);
        }
    }

    private String readInitScriptResource() {
        var path = "sql/init.sql";
        try (InputStream input = plugin.getResource(path)) {
            if (input == null) {
                throw new IllegalStateException("Missing resource: " + path);
            }
            return new String(input.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to read resource: " + path, exception);
        }
    }

    private StandardServiceRegistry buildRegistry(FileConfiguration configuration) {
        var host = configuration.getString("database.host", "localhost");
        var port = configuration.getInt("database.port", 3306);
        var name = configuration.getString("database.name", "rucksack");
        var username = configuration.getString("database.username", "root");
        var password = configuration.getString("database.password", "");
        var parameters = configuration.getString(
                "database.parameters",
                "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
        );
        if (!parameters.isBlank() && parameters.charAt(0) != '?') {
            parameters = "?" + parameters;
        }

        var jdbcUrl = "jdbc:mysql://" + host + ":" + port + "/" + name + parameters;

        return new StandardServiceRegistryBuilder()
                .applySetting("hibernate.connection.driver_class", "com.mysql.cj.jdbc.Driver")
                .applySetting("hibernate.connection.url", jdbcUrl)
                .applySetting("hibernate.connection.username", username)
                .applySetting("hibernate.connection.password", password)
                .applySetting("hibernate.dialect", "org.hibernate.dialect.MySQLDialect")
                .applySetting("hibernate.hbm2ddl.auto", "update")
                .applySetting("hibernate.show_sql", false)
                .applySetting("hibernate.format_sql", false)
                .applySetting("hibernate.type.preferred_uuid_jdbc_type", "CHAR")
                .applySetting(AvailableSettings.CLASSLOADERS, List.of(plugin.getClass().getClassLoader()))
                .applySetting(AvailableSettings.TC_CLASSLOADER, plugin.getClass().getClassLoader())
                .build();
    }

    public void saveBackpackPage(UUID id, int page, byte[] data) {
        Transaction transaction = null;

        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();
            session.merge(BackpackPageEntity.from(id, page, data));
            transaction.commit();
        } catch (Exception exception) {
            if (transaction != null) {
                transaction.rollback();
            }
            plugin.getLogger().severe("Failed to save backpack page " + id + ":" + page + " - " + exception.getMessage());
        }
    }

    public @Nullable byte[] getBackpackPage(UUID id, int page) {
        try (Session session = sessionFactory.openSession()) {
            var result = session.get(BackpackPageEntity.class, new BackpackPageId(id, page));
            if (result == null) {
                return null;
            }
            return result.toRawData();
        } catch (Exception exception) {
            plugin.getLogger().severe("Failed to load backpack page " + id + ":" + page + " - " + exception.getMessage());
            return null;
        }
    }

    @Override
    public void close() {
        if (sessionFactory != null) {
            sessionFactory.close();
        }
        if (serviceRegistry != null) {
            StandardServiceRegistryBuilder.destroy(serviceRegistry);
        }
    }
}



