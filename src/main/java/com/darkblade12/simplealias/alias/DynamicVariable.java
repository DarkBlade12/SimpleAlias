package com.darkblade12.simplealias.alias;

import com.darkblade12.simplealias.replacer.Placeholder;
import com.darkblade12.simplealias.replacer.Replacement;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class DynamicVariable {
    public static Placeholder<String> SENDER_NAME = new Placeholder<>("<sender_name>", Pattern.CASE_INSENSITIVE);
    public static Placeholder<UUID> SENDER_UUID = new Placeholder<>("<sender_uuid>", Pattern.CASE_INSENSITIVE);
    public static Placeholder<String> PARAMS = new Placeholder<>("<params>", Pattern.CASE_INSENSITIVE);
    public static Placeholder<String> PARAMS_INDEX = new Placeholder<>("<params@(\\d+|\\d*-\\d+|\\d+-\\d*)>", Pattern.CASE_INSENSITIVE);
    public static Placeholder<String> WORLD_NAME = new Placeholder<>("<world_name>", Pattern.CASE_INSENSITIVE);
    public static Placeholder<String> MONEY_BALANCE = new Placeholder<>("<money_balance>", Pattern.CASE_INSENSITIVE);
    public static Placeholder<String> GROUP_NAME = new Placeholder<>("<group_name>", Pattern.CASE_INSENSITIVE);
    public static Placeholder<String> FACTION_NAME = new Placeholder<>("<faction_name>", Pattern.CASE_INSENSITIVE);

    public static Placeholder<String> ALIAS_NAME = new Placeholder<>("<alias_name>", Pattern.CASE_INSENSITIVE);
    public static Placeholder<Integer> PARAM_COUNT = new Placeholder<>("<param_count>", Pattern.CASE_INSENSITIVE);
    public static Placeholder<String> REMAINING_TIME = new Placeholder<>("<remaining_time>", Pattern.CASE_INSENSITIVE);
    public static Placeholder<String> COST_AMOUNT = new Placeholder<>("<cost_amount>", Pattern.CASE_INSENSITIVE);

    private DynamicVariable() {
    }

    public static List<Placeholder<?>> all() {
        return Arrays.asList(SENDER_NAME, SENDER_UUID, PARAMS, PARAMS_INDEX, PARAM_COUNT, WORLD_NAME, MONEY_BALANCE, GROUP_NAME,
                             FACTION_NAME, ALIAS_NAME, REMAINING_TIME, COST_AMOUNT);
    }

    public static Replacement<String> createEmptyReplacement() {
        String regex = all().stream().map(p -> String.valueOf(p.getPattern())).collect(Collectors.joining("|", "\\s?(", ")"));
        return new Replacement<>(new Placeholder<>(regex, Pattern.CASE_INSENSITIVE), "");
    }
}
