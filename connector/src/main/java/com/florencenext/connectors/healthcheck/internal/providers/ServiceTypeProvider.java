package com.florencenext.connectors.healthcheck.internal.providers;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.florencenext.connectors.healthcheck.api.model.entities.ServiceType;
import org.mule.runtime.api.value.Value;
import org.mule.runtime.extension.api.values.ValueBuilder;
import org.mule.runtime.extension.api.values.ValueProvider;
import org.mule.runtime.extension.api.values.ValueResolvingException;

public class ServiceTypeProvider implements ValueProvider {
	
	@Override
	public Set<Value> resolve() throws ValueResolvingException {
		List<String> enumStringValues = Arrays.asList(ServiceType.values())
				.stream()
				.map(e -> e.toString())
				.collect(Collectors.toList());

		return ValueBuilder.getValuesFor(enumStringValues);
	}

}
