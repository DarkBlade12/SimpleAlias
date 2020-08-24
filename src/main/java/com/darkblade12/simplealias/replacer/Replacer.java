package com.darkblade12.simplealias.replacer;

import java.util.ArrayList;
import java.util.List;

public final class Replacer {
    private final List<Replacement<?>> replacements;

    Replacer(List<Replacement<?>> replacements) {
        if (replacements.isEmpty()) {
            throw new IllegalArgumentException("Cannot create a replacer without replacements");
        }

        this.replacements = replacements;
    }

    public static ReplacerBuilder builder() {
        return new ReplacerBuilder();
    }

    public String replaceAll(String text) {
        String result = text;
        for (Replacement<?> repl : replacements) {
            result = repl.applyTo(result);
        }
        return result;
    }

    public static final class ReplacerBuilder {
        private final List<Replacement<?>> replacements;

        ReplacerBuilder() {
            replacements = new ArrayList<>();
        }

        private boolean isDuplicate(Replacement<?> replacement) {
            String regex = replacement.getPlaceholder().getPattern().pattern();
            return replacements.stream().map(r -> r.getPlaceholder().getPattern().pattern()).anyMatch(regex::equals);
        }

        public ReplacerBuilder with(Replacement<?> replacement) {
            if (isDuplicate(replacement)) {
                throw new IllegalArgumentException("A replacement with the same pattern has already been added.");
            }

            replacements.add(replacement);
            return this;
        }

        public <T> ReplacerBuilder with(Placeholder<T> placeholder, T value) {
            return with(new Replacement<>(placeholder, value));
        }

        public <T> ReplacerBuilder with(String regex, T value) {
            return with(new Replacement<>(new Placeholder<>(regex), value));
        }

        public <T> ReplacerBuilder with(String regex, int flags, T value) {
            return with(new Replacement<>(new Placeholder<>(regex, flags), value));
        }

        public Replacer build() {
            return new Replacer(replacements);
        }
    }
}
