/*

   ROUX, A foundation library for simple monadic expressions.
   Version 0.2

   Copyright 2011 Kenneth R. Mackenzie (www.mackenzieresearch.com)

   This program is free software: you can redistribute it and/or modify
   it under the terms of Version 3 of the GNU Affero General Public
   License as published by the Free Software Foundation.

   This program is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU Affero General Public License for more details.

   You should have received a copy of the GNU Affero General Public License
   along with this program.  If not, see <http://www.gnu.org/licenses/>.

   If you require a version of this software which can be used as
   part of a commercial for-profit program, please contact Mackenzie
   Research for a commercial license.

   Unless required by applicable law or agreed to in writing, this
   software is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
   CONDITIONS OF ANY KIND, either express or implied.

   See the License for the specific language governing permissions and
   limitations under the License.
*/

package com.mackenzieresearch.roux.context;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.mackenzieresearch.roux.kernel.IReadWriteMonadex;
import com.mackenzieresearch.roux.kernel.IMonad;

public class NullMonadex implements IReadWriteMonadex {

	@Override
	public Object bindValue(String name) {
		return null;
	}

	@Override
	public Object bindValue(int index) {
		return null;
	}

	@Override
	public int getIndex(String name) {
		return -1;
	}

	@Override
	public void returnValue(String name, Object value) {
	}

	@Override
	public void returnValue(int index, Object value) {
	}

	@Override
	public IMonad<?> getMonad(String name) {
		return null;
	}

  @Override
  public boolean containsMonad(String name) {
    return false;
  }

  @Override
  public IMonad<?> getMonad(int index) {
    return null;
  }

  @Override
	public Iterable<IMonad> monads() {
		return new ArrayList<IMonad>();
	}

  @Override
  public IMonad[] getMonads() {
    return new IMonad[0];
  }

  @Override
  public Map<String, IMonad> getVariables() {
    return new HashMap<String, IMonad>();
  }

	@Override
	public int size() {
		return 0;
	}
}
