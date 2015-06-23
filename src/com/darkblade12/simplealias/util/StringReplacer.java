package com.darkblade12.simplealias.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

public final class StringReplacer {
	private final List<String> targets;
	private final List<String> replacements;

	public StringReplacer() {
		targets = new ArrayList<String>();
		replacements = new ArrayList<String>();
	}

	public StringReplacer(String[] targets, String[] replacements) {
		this();
		if (targets.length != replacements.length)
			throw new IllegalArgumentException("Targets and replacements array length do not match");
		for (int i = 0; i < targets.length; i++) {
			this.targets.add(targets[i]);
			this.replacements.add(replacements[i]);
		}
	}

	public void addReplacement(String target, String replacement) {
		if (targets.contains(target))
			throw new IllegalArgumentException("Cannot add another replacement for the same target");
		targets.add(target);
		replacements.add(replacement);
	}

	public boolean removeReplacement(String target) {
		for (int i = 0; i < targets.size(); i++) {
			if (targets.get(i).equals(target)) {
				targets.remove(i);
				replacements.remove(i);
				return true;
			}
		}
		return false;
	}

	public void clearReplacements() {
		targets.clear();
		replacements.clear();
	}

	public String applyReplacement(String s) {
		return StringUtils.replaceEach(s, targets.toArray(new String[targets.size()]), replacements.toArray(new String[replacements.size()]));
	}

	public List<String> getTargets() {
		return Collections.unmodifiableList(targets);
	}

	public List<String> getReplacements() {
		return Collections.unmodifiableList(replacements);
	}
}