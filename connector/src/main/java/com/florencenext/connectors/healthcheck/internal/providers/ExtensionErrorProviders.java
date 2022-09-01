package com.florencenext.connectors.healthcheck.internal.providers;

import java.util.HashSet;
import java.util.Set;

import com.florencenext.connectors.healthcheck.api.model.errors.ExtensionErrorTypes;
import org.mule.runtime.extension.api.annotation.error.ErrorTypeProvider;
import org.mule.runtime.extension.api.error.ErrorTypeDefinition;

public class ExtensionErrorProviders implements ErrorTypeProvider {

	@Override
	public Set<ErrorTypeDefinition> getErrorTypes() {
		// TODO Auto-generated method stub
		 HashSet<ErrorTypeDefinition> errors = new HashSet<ErrorTypeDefinition>();
		 errors.add(ExtensionErrorTypes.HEALTHCHECK_INTERNAL_ERROR);
		return errors;
	}

}
