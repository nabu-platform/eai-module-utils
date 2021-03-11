package nabu.utils.types;

import java.util.List;

import be.nabu.libs.validator.api.Validation;

public class ValidationSummary {
	private String description;
	
	public static ValidationSummary build(List<Validation<?>> validations) {
		StringBuilder builder = new StringBuilder();
		for (Validation<?> validation : validations) {
			builder.append("[" + validation.getSeverity() + "] ");
			if (validation.getCode() != null) {
				builder.append("(" + validation.getCode() + ") ");
			}
			if (validation.getContext() != null && !validation.getContext().isEmpty()) {
				boolean first = true;
				for (int i = validation.getContext().size() - 1; i >= 0; i--) {
					if (first) {
						first = false;
					}
					else {
						builder.append(" > ");
					}
					builder.append(validation.getContext().get(i));
				}
				builder.append(": ");
			}
			builder.append(validation.getMessage());
		}
		ValidationSummary summary = new ValidationSummary();
		summary.setDescription(builder.toString());
		return summary;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
}
