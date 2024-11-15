/*
* Copyright (C) 2014 Alexander Verbruggen
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU Lesser General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public License
* along with this program. If not, see <https://www.gnu.org/licenses/>.
*/

package nabu.utils;

import java.lang.String;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import javax.jws.WebResult;
import javax.jws.WebService;

import be.nabu.eai.repository.EAIResourceRepository;
import be.nabu.libs.artifacts.api.Artifact;
import be.nabu.libs.types.api.ComplexType;
import be.nabu.libs.types.api.DefinedType;

@WebService
public class Repository {
	
	@WebResult(name = "complexTypes")
	public List<ComplexType> getComplexTypes() {
		List<ComplexType> artifacts = new ArrayList<ComplexType>();
		EAIResourceRepository instance = EAIResourceRepository.getInstance();
		if (instance != null) {
			for (DefinedType type : instance.getArtifacts(DefinedType.class)) {
				if (type instanceof ComplexType) {
					artifacts.add((ComplexType) type);
				}
			}
		}
		return artifacts;
	}
	
	@WebResult(name = "complexType")
	public ComplexType getComplexType(String id) throws IOException, ParseException {
		EAIResourceRepository instance = EAIResourceRepository.getInstance();
		Artifact artifact = instance.resolve(id);
		if (artifact != null && artifact instanceof ComplexType) {
			return (ComplexType) artifact;
		}
		return null;
	}
}
