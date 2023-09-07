/*
 *  Copyright (c) 2021 Microsoft Corporation
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Microsoft Corporation - initial API and implementation
 *
 */

package org.eclipse.edc.policy.engine;

import org.eclipse.edc.policy.engine.spi.AtomicConstraintFunction;
import org.eclipse.edc.policy.engine.spi.PolicyContext;
import org.eclipse.edc.policy.engine.spi.PolicyContextImpl;
import org.eclipse.edc.policy.engine.spi.PolicyEngine;
import org.eclipse.edc.policy.engine.spi.RuleFunction;
import org.eclipse.edc.policy.evaluator.PolicyEvaluator;
import org.eclipse.edc.policy.evaluator.RuleProblem;
import org.eclipse.edc.policy.model.Duty;
import org.eclipse.edc.policy.model.Permission;
import org.eclipse.edc.policy.model.Policy;
import org.eclipse.edc.policy.model.Prohibition;
import org.eclipse.edc.policy.model.Rule;
import org.eclipse.edc.spi.agent.ParticipantAgent;
import org.eclipse.edc.spi.result.Result;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.BiFunction;

import static java.util.stream.Collectors.toList;
import static org.eclipse.edc.spi.result.Result.failure;
import static org.eclipse.edc.spi.result.Result.success;

/**
 * Default implementation of the policy engine.
 */
public class PolicyEngineImpl implements PolicyEngine {

    private static final String ALL_SCOPES_DELIMITED = ALL_SCOPES + DELIMITER;

    private final Map<String, List<ConstraintFunctionEntry<Rule>>> constraintFunctions = new TreeMap<>();
    private final Map<String, List<RuleFunctionEntry<Rule>>> ruleFunctions = new TreeMap<>();
    private final Map<String, List<BiFunction<Policy, PolicyContext, Boolean>>> preValidators = new HashMap<>();
    private final Map<String, List<BiFunction<Policy, PolicyContext, Boolean>>> postValidators = new HashMap<>();
    private final ScopeFilter scopeFilter;

    public PolicyEngineImpl(ScopeFilter scopeFilter) {
        this.scopeFilter = scopeFilter;
    }

    @Override
    public Policy filter(Policy policy, String scope) {
        return scopeFilter.applyScope(policy, scope);
    }

    @Override
    public Result<Void> evaluate(String scope, Policy policy, PolicyContext context) {
        var delimitedScope = scope + ".";

        var scopedPreValidators = preValidators.entrySet().stream().filter(entry -> scopeFilter(entry.getKey(), delimitedScope)).flatMap(l -> l.getValue().stream()).toList();
        for (var validator : scopedPreValidators) {
            if (!validator.apply(policy, context)) {
                return failValidator("Pre-validator", validator, context);
            }
        }

        var evalBuilder = PolicyEvaluator.Builder.newInstance();

        ruleFunctions.entrySet().stream().filter(entry -> scopeFilter(entry.getKey(), delimitedScope)).flatMap(entry -> entry.getValue().stream()).forEach(entry -> {
            if (Duty.class.isAssignableFrom(entry.type)) {
                evalBuilder.dutyRuleFunction((rule) -> entry.function.evaluate(rule, context));
            } else if (Permission.class.isAssignableFrom(entry.type)) {
                evalBuilder.permissionRuleFunction((rule) -> entry.function.evaluate(rule, context));
            } else if (Prohibition.class.isAssignableFrom(entry.type)) {
                evalBuilder.prohibitionRuleFunction((rule) -> entry.function.evaluate(rule, context));
            }
        });

        constraintFunctions.entrySet().stream().filter(entry -> scopeFilter(entry.getKey(), delimitedScope)).flatMap(entry -> entry.getValue().stream()).forEach(entry -> {
            if (Duty.class.isAssignableFrom(entry.type)) {
                evalBuilder.dutyFunction(entry.key, (operator, value, duty) -> entry.function.evaluate(operator, value, duty, context));
            } else if (Permission.class.isAssignableFrom(entry.type)) {
                evalBuilder.permissionFunction(entry.key, (operator, value, permission) -> entry.function.evaluate(operator, value, permission, context));
            } else if (Prohibition.class.isAssignableFrom(entry.type)) {
                evalBuilder.prohibitionFunction(entry.key, (operator, value, prohibition) -> entry.function.evaluate(operator, value, prohibition, context));
            }
        });

        var evaluator = evalBuilder.build();

        var filteredPolicy = scopeFilter.applyScope(policy, scope);

        var result = evaluator.evaluate(filteredPolicy);

        if (result.valid()) {

            var scopedPostValidators = postValidators.entrySet().stream().filter(entry -> scopeFilter(entry.getKey(), delimitedScope)).flatMap(l -> l.getValue().stream()).toList();
            for (var validator : scopedPostValidators) {
                if (!validator.apply(policy, context)) {
                    return failValidator("Post-validator", validator, context);
                }
            }

            return success();
        } else {
            return failure(result.getProblems().stream().map(RuleProblem::getDescription).collect(toList()));
        }
    }

    @Override
    @Deprecated(since = "0.1.1")
    public Result<Policy> evaluate(String scope, Policy policy, ParticipantAgent agent) {
        var context = PolicyContextImpl.Builder.newInstance().additional(ParticipantAgent.class, agent).build();
        return evaluate(scope, policy, context).map(it -> policy);
    }

    @Override
    @Deprecated(since = "0.1.1")
    public Result<Policy> evaluate(String scope, Policy policy, ParticipantAgent agent, Map<Class<?>, Object> contextInformation) {
        var builder = PolicyContextImpl.Builder.newInstance()
                .additional(ParticipantAgent.class, agent);

        contextInformation.forEach((key, value) -> builder.additional(key, key.cast(value)));

        return evaluate(scope, policy, builder.build()).map(it -> policy);
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public <R extends Rule> void registerFunction(String scope, Class<R> type, String key, AtomicConstraintFunction<R> function) {
        constraintFunctions.computeIfAbsent(scope + ".", k -> new ArrayList<>()).add(new ConstraintFunctionEntry(type, key, function));
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public <R extends Rule> void registerFunction(String scope, Class<R> type, RuleFunction<R> function) {
        ruleFunctions.computeIfAbsent(scope + ".", k -> new ArrayList<>()).add(new RuleFunctionEntry(type, function));
    }

    @Override
    public void registerPreValidator(String scope, BiFunction<Policy, PolicyContext, Boolean> validator) {
        preValidators.computeIfAbsent(scope + DELIMITER, k -> new ArrayList<>()).add(validator);
    }

    @Override
    public void registerPostValidator(String scope, BiFunction<Policy, PolicyContext, Boolean> validator) {
        postValidators.computeIfAbsent(scope + DELIMITER, k -> new ArrayList<>()).add(validator);
    }

    private boolean scopeFilter(String entry, String scope) {
        return ALL_SCOPES_DELIMITED.equals(entry) || scope.startsWith(entry);
    }

    @NotNull
    private Result<Void> failValidator(String type, BiFunction<Policy, PolicyContext, Boolean> validator, PolicyContext context) {
        return failure(context.hasProblems() ? context.getProblems() : List.of(type + " failed: " + validator.getClass().getName()));
    }

    private static class ConstraintFunctionEntry<R extends Rule> {
        Class<R> type;
        String key;
        AtomicConstraintFunction<R> function;

        ConstraintFunctionEntry(Class<R> type, String key, AtomicConstraintFunction<R> function) {
            this.type = type;
            this.key = key;
            this.function = function;
        }
    }

    private static class RuleFunctionEntry<R extends Rule> {
        Class<R> type;
        RuleFunction<R> function;

        RuleFunctionEntry(Class<R> type, RuleFunction<R> function) {
            this.type = type;
            this.function = function;
        }
    }

}
