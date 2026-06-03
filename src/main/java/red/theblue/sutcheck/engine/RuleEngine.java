package red.theblue.sutcheck.engine;

import red.theblue.sutcheck.model.PrescribedItem;
import red.theblue.sutcheck.model.Report;

import java.util.ArrayList;
import java.util.List;

/**
 * Kayitli kurallari raporun her kalemine uygular.
 * Bir kaleme hicbir kural uymazsa, "tanimli kural yok -> manuel inceleme" doner;
 * boylece sistem sessizce yanlis "uygun" demez (guvenli varsayilan).
 */
public class RuleEngine {

    private final List<Rule> rules = new ArrayList<>();

    public RuleEngine register(Rule rule) {
        rules.add(rule);
        return this;
    }

    public List<RuleResult> evaluate(Report report) {
        List<RuleResult> results = new ArrayList<>();
        for (PrescribedItem item : report.items) {
            boolean matched = false;
            for (Rule rule : rules) {
                if (rule.appliesTo(item, report)) {
                    results.add(rule.evaluate(item, report));
                    matched = true;
                }
            }
            if (!matched) {
                results.add(RuleResult.needsReview(item,
                        "Bu kalem icin tanimli bir SUT kurali bulunamadi; manuel inceleme gerekir.",
                        "-"));
            }
        }
        return results;
    }
}
