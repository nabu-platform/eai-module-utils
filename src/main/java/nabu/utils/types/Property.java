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

package nabu.utils.types;

import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import be.nabu.libs.types.utils.KeyValuePairImpl;

import java.lang.String;

@XmlRootElement
@XmlType(propOrder = { "key", "value" })
public class Property extends KeyValuePairImpl {

	public Property() {
		// automatic creation
	}
	
	public Property(String key, String value) {
		super(key, value);
	}

	@NotNull
	public String getKey() {
		return super.getKey();
	}

	@Override
	public String getValue() {
		return super.getValue();
	}

	@Override
	public void setKey(String key) {
		super.setKey(key);
	}

	@Override
	public void setValue(String value) {
		super.setValue(value);
	}

}
