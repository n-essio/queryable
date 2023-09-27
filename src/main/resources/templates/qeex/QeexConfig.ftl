package ${groupId}.api.qeex.annotations;


import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithName;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

@ConfigMapping(prefix = "qeex")
public interface QeexConfig {

    @WithName("project")
    Optional<String> project();

    @WithName("default.id")
    Optional<Integer> default_id();

    @WithName("default.code")
    Optional<Integer> default_code();

    @WithName("default.language")
    Optional<String> default_language();

    @WithName("default.message")
    Optional<String> default_message();

    @WithName("messages")
    Set<Message> messages();

    interface Message {
        Optional<String> message();

        Optional<Integer> code();

        Optional<Integer> id();

        Map<String, String> language();
    }

    default Integer get_code(int id, Integer code) {
        if (code == null) {
            return default_code().get();
        }
        for (Message msg : messages()) {
            if (msg.id().isPresent() && msg.id().get().equals(id)) {
                if (msg.code().isPresent()) {
                    return msg.code().get();
                }
            }
        }
        return code;
    }

    default String get_message(int id, String message, String language) {
        var lang = default_language().get();
        if (language != null) {
            lang = language;
        }
        for (Message msg : messages()) {
            if (msg.id().isPresent() && msg.id().get().equals(id)) {
                if (lang != null && msg.language().containsKey(language)) {
                    return msg.language().get(language);
                }
                if (msg.message().isPresent()) {
                    return msg.message().get();
                }
            }
        }
        return message;
    }

    default String get_language(int id, String language) {
        if (language == null) {
            return default_language().get();
        }
        for (Message msg : messages()) {
            if (msg.id().isPresent() && msg.id().get().equals(id)) {
                if (msg.message().isPresent()) {
                    if (msg.language().containsKey(language)) {
                        return language;
                    }
                }
            }
        }
        return default_language().get();
    }

    default String get_project(String project) {
        return project().orElse(project);
    }
}
