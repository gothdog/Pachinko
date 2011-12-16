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

import com.mackenzieresearch.roux.kernel.*;

import java.util.ArrayList;
import java.util.List;

public class Monad<T> implements IMonad<T> {
	private String _name = null;
	protected T _value = null;
  List<IListener> _changeListeners = new ArrayList<IListener>();
  List<IListener> _reversionListeners = new ArrayList<IListener>();

	public Monad(String name) {
		_name = name;
	}

  public Monad(String name, int key) {
    this(name);
  }

  public Monad(String name, int key, T activationValue) {
    this(name, key);
  }

	@Override
	public T bindValue(IMonadex<? extends IReadOnlyMonad> context) {
		return _value;
	}

  @Override
	public String getName() {
		return _name;
	}

	@Override
  public void returnValue(T value, IMonadex<? extends IMonad> context) {
		_value = value;
    for (IListener listener : _changeListeners)
      listener.changed(this, context);
	}

	@SuppressWarnings("unchecked")
	@Override
  public void returnObject(Object value, IMonadex<? extends IMonad> context) {
		_value = (T) value;
    for (IListener listener : _changeListeners)
      listener.changed(this, context);
	}

  @Override
  public void addChangeListener(IListener iReadOnlyMonadIListener) {
    _changeListeners.add(iReadOnlyMonadIListener);
  }

  @Override
  public List<IListener> getChangeListeners() {
    return _changeListeners;
  }

  @Override
  public void addReversionListener(IListener iReadOnlyMonadIListener) {
    _reversionListeners.add(iReadOnlyMonadIListener);
  }

  @Override
  public List<IListener> getReversionListeners() {
    return _reversionListeners;
  }

}
