/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.jpamodelgen.annotation;

import org.hibernate.jpamodelgen.model.Metamodel;

import java.util.List;

/**
 * @author Gavin King
 */
public class NaturalIdFinderMethod extends AbstractFinderMethod {

	public NaturalIdFinderMethod(
			Metamodel annotationMetaEntity,
			String methodName, String entity,
			List<String> paramNames, List<String> paramTypes,
			boolean belongsToDao,
			String sessionType,
			String sessionName,
			List<String> fetchProfiles,
			boolean addNonnullAnnotation) {
		super( annotationMetaEntity, methodName, entity, belongsToDao, sessionType, sessionName, fetchProfiles,
				paramNames, paramTypes, addNonnullAnnotation );
	}

	@Override
	boolean isId() {
		// natural ids can be null
		return false;
	}

	@Override
	public String getAttributeDeclarationString() {
		final StringBuilder declaration = new StringBuilder();
		comment( declaration );
		preamble( declaration );
		unwrapSession( declaration );
		if ( reactive ) {
			findReactively( declaration );
		}
		else {
			findBlockingly( declaration );
		}
		declaration.append(";\n}");
		return declaration.toString();
	}

	private void findBlockingly(StringBuilder declaration) {
		declaration
				.append(".byNaturalId(")
				.append(annotationMetaEntity.importType(entity))
				.append(".class)");
		enableFetchProfile( declaration );
		for ( int i = 0; i < paramNames.size(); i ++ ) {
			final String paramName = paramNames.get(i);
			declaration
					.append("\n\t\t\t.using(")
					.append(annotationMetaEntity.importType(entity + '_'))
					.append('.')
					.append(paramName)
					.append(", ")
					.append(paramName)
					.append(")");
		}
		declaration
				.append("\n\t\t\t.load()");
	}

	private void findReactively(StringBuilder declaration) {
		boolean composite = paramNames.size() > 1;
		declaration
				.append(".find(");
		if (composite) {
			declaration.append("\n\t\t\t");
		}
		declaration
				.append(annotationMetaEntity.importType(entity))
				.append(".class, ");
		if (composite) {
			declaration
					.append("\n\t\t\t")
					.append(annotationMetaEntity.importType("org.hibernate.reactive.common.Identifier"))
					.append(".composite(");
		}
		for ( int i = 0; i < paramNames.size(); i ++ ) {
			if ( i>0 ) {
				declaration
						.append(", ");
			}
			if (composite) {
				declaration
						.append("\n\t\t\t\t");
			}
			final String paramName = paramNames.get(i);
			declaration
					.append(annotationMetaEntity.importType("org.hibernate.reactive.common.Identifier"))
					.append(".id(")
					.append(annotationMetaEntity.importType(entity + '_'))
					.append('.')
					.append(paramName)
					.append(", ")
					.append(paramName)
					.append(")");
		}
		if (composite) {
			declaration.append("\n\t\t\t)\n\t");
		}
		declaration.append(")");
	}

}
