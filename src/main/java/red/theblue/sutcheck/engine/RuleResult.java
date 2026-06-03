package red.theblue.sutcheck.engine;

import red.theblue.sutcheck.model.PrescribedItem;

/** Tek bir kalem icin tek bir kuralin urettigi sonuc. */
public class RuleResult {
    public final PrescribedItem item;
    public final ComplianceStatus status;
    public final String reason;        // insan-okur gerekce
    public final String sutReference;  // ilgili SUT maddesi, orn. "SUT 4.2.38 (6)"

    public RuleResult(PrescribedItem item, ComplianceStatus status, String reason, String sutReference) {
        this.item = item;
        this.status = status;
        this.reason = reason;
        this.sutReference = sutReference;
    }

    public static RuleResult compliant(PrescribedItem item, String reason, String ref) {
        return new RuleResult(item, ComplianceStatus.COMPLIANT, reason, ref);
    }

    public static RuleResult nonCompliant(PrescribedItem item, String reason, String ref) {
        return new RuleResult(item, ComplianceStatus.NON_COMPLIANT, reason, ref);
    }

    public static RuleResult needsReview(PrescribedItem item, String reason, String ref) {
        return new RuleResult(item, ComplianceStatus.NEEDS_REVIEW, reason, ref);
    }
}
