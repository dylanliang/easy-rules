/*
 * The MIT License
 *
 *  Copyright (c) 2015, Mahmoud Ben Hassine (mahmoud@benhassine.fr)
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in
 *  all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *  THE SOFTWARE.
 */

package org.easyrules.core;

import org.easyrules.api.Rule;
import org.easyrules.util.EasyRulesConstants;

import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Default {@link org.easyrules.api.RulesEngine} implementation.
 *
 * This implementation handles a set of rules with unique name.
 *
 * Rules are fired according to their natural order which is priority by default.
 *
 * @author Mahmoud Ben Hassine (mahmoud@benhassine.fr)
 */
public class DefaultRulesEngine extends AbstractRulesEngine<Rule> {

    private static final Logger LOGGER = Logger.getLogger(DefaultRulesEngine.class.getName());

    /**
     * Construct a default rules engine with default values.
     */
    public DefaultRulesEngine() {
        this(false, EasyRulesConstants.DEFAULT_RULE_PRIORITY_THRESHOLD);
    }

    /**
     * Constructs a default rules engine.
     * @param skipOnFirstAppliedRule true if the engine should skip next rule after the first applied rule
     */
    public DefaultRulesEngine(boolean skipOnFirstAppliedRule) {
        this(skipOnFirstAppliedRule, EasyRulesConstants.DEFAULT_RULE_PRIORITY_THRESHOLD);
    }

    /**
     * Constructs a default rules engine.
     * @param rulePriorityThreshold rule priority threshold
     */
    public DefaultRulesEngine(int rulePriorityThreshold) {
        this(false, rulePriorityThreshold);
    }

    /**
     * Constructs a default rules engine.
     * @param skipOnFirstAppliedRule true if the engine should skip next rule after the first applied rule
     * @param rulePriorityThreshold rule priority threshold
     */
    public DefaultRulesEngine(boolean skipOnFirstAppliedRule, int rulePriorityThreshold) {
        rules = new TreeSet<Rule>();
        this.skipOnFirstAppliedRule = skipOnFirstAppliedRule;
        this.rulePriorityThreshold = rulePriorityThreshold;
    }

    @Override
    public void fireRules() {

        if (rules.isEmpty()) {
            LOGGER.warning("No rules registered! Nothing to apply.");
            return;
        }

        logEngineParameters();

        applyRules();

    }

    protected void applyRules() {

        for (Rule rule : rules) {

            final String ruleName = rule.getName();

            final int rulePriority = rule.getPriority();
            if (rulePriority > rulePriorityThreshold) {
                LOGGER.log(Level.INFO,
                        "Rule priority threshold {0} exceeded at rule ''{1}'' (priority={2}), next applicable rules will be skipped.",
                        new Object[] {rulePriorityThreshold, ruleName, rulePriority});
                break;
            }

            final boolean shouldApplyRule = rule.evaluateConditions();
            if (shouldApplyRule) {
                LOGGER.log(Level.INFO, "Rule ''{0}'' triggered.", ruleName);
                try {
                    rule.performActions();
                    LOGGER.log(Level.INFO, "Rule ''{0}'' performed successfully.", ruleName);

                    if (skipOnFirstAppliedRule) {
                        LOGGER.info("Next rules will be skipped according to parameter skipOnFirstAppliedRule.");
                        break;
                    }
                } catch (Exception exception) {
                    LOGGER.log(Level.SEVERE, String.format("Rule '%s' performed with error.", ruleName), exception);
                }
            }

        }

    }

}
