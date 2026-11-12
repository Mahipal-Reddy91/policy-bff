package com.abc.apac.policybff.api.converter;

import com.abc.apac.policybff.domain.entity.LineOfBusiness;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class LineOfBusinessConverter implements Converter<String, LineOfBusiness> {
	@Override
	public LineOfBusiness convert(String source) {
		return LineOfBusiness.fromValue(source.trim());
	}
}
