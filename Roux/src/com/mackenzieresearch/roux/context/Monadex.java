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

import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static util.RuntimeAssertion.assertNotNull;

public class Monadex<M extends IMonad<?>> implements IMonadex<M>, IListener<M>, ISummarizedMonadex<M>,
        IListenable, IExtensibleMonadex<M> {

  protected Map<String, Integer> _monadIndex = new HashMap<String, Integer>();
  protected List<M> _monadList = new ArrayList<M>();

  protected ArrayList<M> _changed = new ArrayList<M>();
  protected List<IListener> _changeListeners = new ArrayList<IListener>();
  protected List<IListener> _reversionListeners = new ArrayList<IListener>();


  public Monadex(M... monads) {
    _addMonads(monads);
  }

  @Override
  public int getIndex(String name) {
    Integer index = _monadIndex.get(name);
    if (index != null && index >= 0 && index < _monadList.size())
      return index;
    else
      return -1;
  }

  @Override
  public void returnValue(String name, Object value) {
    int index = _monadIndex.get(name);
    assert (index >= 0 && index < _monadList.size());

    IMonad cref = _monadList.get(index);
    if (cref != null) {
      cref.returnObject(value, this);
    }
  }

  @Override
  public void returnValue(int index, Object value) {
    assert (index >= 0 && index < _monadList.size());

    IMonad<?> cref = _monadList.get(index);
    if (cref != null) {
      cref.returnObject(value, this);
    }
  }

  @Override
  public Object bindValue(String name) {
    int index = _monadIndex.get(name);
    assert (index >= 0 && index < _monadList.size());

    IMonad cref = _monadList.get(index);
    if (cref != null)
      return cref.bindValue(this);
    else
      return null;
  }

  @Override
  public Object bindValue(int index) {
    assert (index >= 0 && index < _monadList.size());
    return _monadList.get(index).bindValue(this);
  }

  @Override
  public boolean containsMonad(String name) {
    Integer index = _monadIndex.get(name);
    return index != null && index >= 0 && index < _monadList.size();
  }

  @Override
  public M getMonad(String name) {
    Integer index = _monadIndex.get(name);
    if (index != null)
      return _monadList.get(index);
    else return null;
  }

  @Override
  public M getMonad(int index) {
    assert (index >= 0 && index < _monadList.size());
    return _monadList.get(index);
  }

  @SuppressWarnings("unchecked")
  public void setMonad(M monad) {
    int index = _monadIndex.get(monad.getName());
    assert (index >= 0 && index < _monadList.size());
    _monadList.set(index, monad);
    monad.addChangeListener(this);
  }

  //  This version is used internally when the change listener mechanism is being sidestepped...
  protected void _setMonad(M monad) {
    int index = _monadIndex.get(monad.getName());
    assert (index >= 0 && index < _monadList.size());
    _monadList.set(index, monad);
  }

  @Override
  public Iterable<M> monads() {
    ArrayList<M> monads = new ArrayList<M>(_monadList);
    return monads;
  }

  @Override
  public M[] getMonads() {
    return (M[]) _monadList.toArray(new IMonad<?>[_monadList.size()]);
  }

  @Override
  public Map<String, M> getVariables() {
    Map<String, M> vars = new HashMap<String, M>();

    for (String key : _monadIndex.keySet()) {
      int index = _monadIndex.get(key);
      vars.put(key, _monadList.get(index));
    }

    return vars;
  }

  @Override
  public int size() {
    return _monadList.size();
  }

  @Override
  public void addChangeListener(IListener listener) {
    assert (listener != null);
    _changeListeners.add(listener);
  }


  @Override
  public List<IListener> getChangeListeners() {
    return _changeListeners;
  }

  @Override
  public void addReversionListener(IListener listener) {
    assert (listener != null);
    _reversionListeners.add(listener);
  }

  @Override
  public List<IListener> getReversionListeners() {
    return _reversionListeners;
  }

  @Override
  public List<M> getChanged() {
    return _changed;
  }

  @Override
  public List<M> getUnchanged() {
    ArrayList<M> unchanged = new ArrayList<M>();
    for (M cref : _monadList) {
      if (!_changed.contains(cref)) {
//        IMonad indirectlyReferencedCRef = _readWriteContext.getMonad(cref.getName());
//        if (!_changed.contains(indirectlyReferencedCRef)) {
        unchanged.add(cref);
//        }
      }
    }

    return unchanged;
  }

  @Override
  public void changed(M ref, IMonadex<? extends IReadOnlyMonad> context) {
    _changed.add(ref);

    for (IListener<IMonadex<M>> listener : _changeListeners)
      listener.changed(this, context);
  }

  @Override
  public void added(M ref, IMonadex<? extends IReadOnlyMonad> context) {
    // TODO Auto-generated method stub
  }

  @Override
  public void removed(M ref, IMonadex<? extends IReadOnlyMonad> context) {
    // TODO Auto-generated method stub
  }

  @Override
  public void reverted(M ref, IMonadex<? extends IReadOnlyMonad> context) {
    // TODO Auto-generated method stub
  }

  private void _addMonads(M... monads) {
    for (M monad : monads)
      defineVariableBinding(monad);
  }

  protected boolean _addMonad(M monad) {
    boolean added = false;

    if (!_monadIndex.containsKey(monad.getName())) {
      _monadList.add(monad);
      _monadIndex.put(monad.getName(), (_monadList.size() - 1));
      added = true;
    }

    return added;
  }

  @Override
  public void defineVariableBinding(M monad) {
    if (_addMonad(monad))
      monad.addChangeListener(this);
    else
      throw new IllegalArgumentException("Monad " + monad.getName() + " not added.  Already exists in this Monadex.");
  }

  @Override
  public void defineVariableBindings(IMonadex<M> readWriteContext) {
    for (M cref : readWriteContext.monads())
      defineVariableBinding(cref);
  }
}
