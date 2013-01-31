/*
 * @(#)MetadataTemplate.java
 *
 * Copyright 2011 Instituto Superior Tecnico
 * Founding Authors: Luis Cruz, Sérgio Silva
 * 
 *      https://fenix-ashes.ist.utl.pt/
 * 
 *   This file is part of the File Management Module.
 *
 *   The File Management Module is free software: you can
 *   redistribute it and/or modify it under the terms of the GNU Lesser General
 *   Public License as published by the Free Software Foundation, either version 
 *   3 of the License, or (at your option) any later version.
 *
 *   The File Management  Module is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *   GNU Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public License
 *   along with the File Management  Module. If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package module.fileManagement.domain.metadata;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import module.fileManagement.domain.FileManagementSystem;
import pt.ist.fenixWebFramework.services.Service;
import pt.ist.fenixframework.pstm.AbstractDomainObject;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Sets;

/**
 * 
 * @author Sérgio Silva
 * 
 */
public class MetadataTemplate extends MetadataTemplate_Base {

	final static Function<MetadataTemplateRule, MetadataKey> RULE_KEY_FUNCTION =
			new Function<MetadataTemplateRule, MetadataKey>() {

				@Override
				public MetadataKey apply(MetadataTemplateRule arg0) {
					return arg0.getKey();
				}
			};

	public MetadataTemplate() {
		super();
		FileManagementSystem.getInstance().addMetadataTemplates(this);
	}

	public MetadataTemplate(String name) {
		this();
		setName(name);
	}

	public MetadataTemplate(String name, Set<String> keys) {
		this();
		setName(name);
		addKeysStrings(keys);
	}

	public static MetadataTemplate getMetadataTemplate(String name) {
		for (MetadataTemplate template : FileManagementSystem.getInstance().getMetadataTemplates()) {
			if (template.getName().equals(name)) {
				return template;
			}
		}
		return null;
	}

	@Service
	public void delete() {
		getKeys().clear();
		for (MetadataTemplateRule rule : getRule()) {
			rule.delete();
		}
		for (MetadataTemplate child : getChilds()) {
			removeChilds(child);
		}
		setFileManagementSystem(null);
		deleteDomainObject();
	}

	private void removeKey(MetadataKey key) {
		final MetadataTemplateRule rule = getRule(key);
		if (rule != null) {
			rule.delete();
		}
	}

	public MetadataTemplateRule getRule(final MetadataKey key) {
		for (MetadataTemplateRule rule : getRule()) {
			if (rule.getKey().equals(key)) {
				return rule;
			}
		}
		return null;
	}

	@Service
	public static MetadataTemplate getOrCreateInstance(String name) {
		MetadataTemplate instance = getMetadataTemplate(name);
		if (instance == null) {
			instance = new MetadataTemplate(name);
		}
		return instance;
	}

	@Service
	public void addKeysStrings(Set<String> keys) {
		for (String key : keys) {
			addKey(MetadataKey.getOrCreateInstance(key), getRule().size() + 1);
		}
	}

	private void addKey(MetadataKey key, Integer position) {
		addKey(key, position, Boolean.FALSE, Boolean.FALSE);
	}

	public void addKey(MetadataKey key, Integer position, Boolean required) {
		addKey(key, position, required, Boolean.FALSE);
	}

	@Service
	public void addKey(MetadataKey key, Integer position, Boolean required, Boolean readOnly) {
		addRule(new MetadataTemplateRule(this, key, position, required, readOnly));
	}

	public Set<String> getKeyNames() {
		return FluentIterable.from(getKey()).transform(new Function<MetadataKey, String>() {

			@Override
			public String apply(MetadataKey arg0) {
				return arg0.getKeyValue();
			}
		}).toImmutableSet();
	}

	@SuppressWarnings("unchecked")
	public static MetadataTemplate fromExternalId(String templateOid) {
		if (templateOid == null || templateOid.isEmpty()) {
			return null;
		} else {
			return AbstractDomainObject.fromExternalId(templateOid);
		}
	}

	public MetadataKey getKey(final MetadataKey key) {
		final Iterator<MetadataKey> iterator = getKeysFluentIterable().filter(new Predicate<MetadataKey>() {

			@Override
			public boolean apply(MetadataKey argKey) {
				return argKey.equals(key);
			}
		}).iterator();
		return iterator.hasNext() ? iterator.next() : null;
	}

	public boolean hasKey(final MetadataKey key) {
		return getKey(key) != null;
	}

	public Set<MetadataKey> getKey() {
		return getPositionOrderedKeys();
	}

	private FluentIterable<MetadataKey> getKeysFluentIterable() {
		return FluentIterable.from(getRuleSet()).transform(RULE_KEY_FUNCTION);
	}

	public Set<MetadataKey> getPositionOrderedKeys() {
		return FluentIterable.from(Sets.newTreeSet(getRuleSet())).transform(RULE_KEY_FUNCTION).toImmutableSet();
	}

	public List<MetadataKey> getPositionOrderedKeysList() {
		return FluentIterable.from(Sets.newTreeSet(getRuleSet())).transform(RULE_KEY_FUNCTION).toImmutableList();
	}

	public TreeSet<MetadataTemplateRule> getPositionOrderedRules() {
		return Sets.newTreeSet(getRuleSet());
	}

	public Set<MetadataKey> getKeysByMetadataType(final Class<? extends Metadata> metadata) {
		return getKeysFluentIterable().filter(new Predicate<MetadataKey>() {
			@Override
			public boolean apply(MetadataKey key) {
				return key.getMetadataValueType().equals(metadata);
			}
		}).toImmutableSet();
	}

	@Service
	public void clear() {
		Set<MetadataTemplateRule> rules = Sets.newHashSet(getRuleSet());
		for (MetadataTemplateRule rule : rules) {
			rule.delete();
		}
	}

	public boolean hasAnyKey() {
		return hasAnyRule();
	}

	@Override
	public String toString() {
		return getName();
	}
}
