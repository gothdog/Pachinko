/*
   PACHINKO, A fast, embeddable micro-rule engine runtime.
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

package com.mackenzieresearch.pachinko;

import com.mackenzieresearch.roux.context.Monadex;
import com.mackenzieresearch.roux.kernel.*;

import java.util.ArrayList;
import java.util.List;

public class ActivationContext<P, R extends IBehavior> extends Monadex<IMonad<P>> {
  protected R _rule = null;
  protected int _variableRefCount = 0;
  protected int _pkVariableCount = 0;
  protected List<MeasureVariable> _vars = new ArrayList<MeasureVariable>();


  public ActivationContext(R rule) {
    super();
    _rule = rule;
  }

  public R getRule() {
    return _rule;
  }

  public boolean isActivatable() {
    return _variableRefCount <= 0;
  }

  @Override
  public void setMonad(IMonad<P> monad) {
    //  First update the PK's to point to the new monad...
    for (MeasureVariable pkVariable : _vars) {
      if (pkVariable.setMonad(monad))
        break;
    }
    //  Then let the underlying Monadex have it:
    _setMonad(monad);
  }

  public void defineOptionalVariableBinding(IMonad<P> monad) {
    if (_addMonad(monad)) {
      MeasureVariable pkvar = new MeasureVariable(monad);
      _vars.add(pkvar);
    }
  }

  @Override
  public void defineVariableBinding(IMonad<P> monad) {
    if (_addMonad(monad)) {
      MeasureVariable pkvar = new MeasureVariable(monad);
      pkvar.addChangeListener(this);
      _vars.add(pkvar);
      _variableRefCount = _variableRefCount + 1;
    }
  }

  public void defineVariableBinding(IMonad<P> monad, Object activationValue) {
    defineVariableBinding(monad, activationValue, true);
  }

  public void defineVariableBinding(IMonad<P> monad, Object activationValue, boolean useActivationValue) {
    if (_addMonad(monad)) {
      MeasureVariable pkvar;
      if (useActivationValue)
        pkvar = new MeasureVariable(monad, activationValue);
      else
        pkvar = new MeasureVariable(monad);
      pkvar.addChangeListener(this);
      _vars.add(pkvar);
      _variableRefCount = _variableRefCount + 1;
    }
  }

  public void definePKVariableBinding(IMonad<P> monad) {
    definePKVariableBinding(monad, null, false);
  }

  public void definePKVariableBinding(IMonad<P> monad, Object activationValue) {
    definePKVariableBinding(monad, activationValue, true);
  }

  public void definePKVariableBinding(IMonad<P> monad, Object activationValue, boolean useActivationValue) {
    if (_addMonad(monad)) {
      MeasureVariable pkvar;
      if (useActivationValue)
        pkvar = new PKVariable(monad, activationValue);
      else
        pkvar = new PKVariable(monad);
      pkvar.addChangeListener(this);
      _vars.add(pkvar);
      _variableRefCount = _variableRefCount + 1;
      _pkVariableCount = _pkVariableCount + 1;
    }
  }

  public void resetKeyFieldRefCount() {
    _variableRefCount = _monadList.size();
    for (MeasureVariable var : _vars)
      var.deactivate();
  }

  @Override
  public void changed(IMonad<P> ref, IMonadex<? extends IReadOnlyMonad> context) {
    //
    if (isActivatable()) {
      super.changed(ref, context);
    }
  }

  public class PKVariable<M extends IMonad<?>> extends MeasureVariable<M> {

    public PKVariable(M monad) {
      super(monad);
    }

    public PKVariable(M monad, Object activationValue) {
      super(monad, activationValue);
    }

    @Override
    public void changed(M ref, IMonadex<? extends IReadOnlyMonad> context) {
      //  Only consider activating if we are not already activated...
      if (!_activated) {

        //  If this PKVariable has an activation value associated with it, verify
        //  that the current value of the variable satisfies the activation value...
        if (_useActivationValue) {
          Object value = ref.bindValue(context);
          if (_activationValue == null && !(value == null))
            return;
          else if (!value.equals(_activationValue))
            return;
        }

        //  Don't activate again until the ActivationContext decides all criteria have been met:
        _activated = true;

        //  Let the ActivationContext know it is one variable closer to activating:
        _variableRefCount = _variableRefCount - 1;
      }

      if (_activated) {
        //  Share the good news with whoever is listening.  (Likely only the ActivationContext)...
        for (IListener listener : _changeListeners)
          listener.changed(ref, context);
      }
    }
  }

  public class MeasureVariable<M extends IMonad<?>> implements IListener<M>, IListenable {
    protected M _monad = null;
    protected boolean _activated = false;
    protected List<IListener> _changeListeners = new ArrayList<IListener>();
    protected List<IListener> _reversionListeners = new ArrayList<IListener>();
    protected Object _activationValue = null;
    protected boolean _useActivationValue = false;

    public MeasureVariable(M monad) {
      _monad = monad;
      monad.addChangeListener(this);
    }

    public MeasureVariable(M monad, Object activationValue) {
      this(monad);
      _activationValue = activationValue;
      _useActivationValue = true;
    }

    public boolean setMonad(M monad) {
      if (_monad.getName().equals(monad.getName())) {
        _monad = monad;
        monad.addChangeListener(this);
        return true;
      } else
        return false;
    }

    public void deactivate() {
      _activated = false;
    }

    @Override
    public void changed(M ref, IMonadex<? extends IReadOnlyMonad> context) {

      //  If this variable has an activation value associated with it, verify
      //  that the current value of the variable satisfies the activation value...
      if (_useActivationValue) {
        Object value = ref.bindValue(context);
        if (_activationValue == null && !(value == null))
          return;
        else if (!value.equals(_activationValue))
          return;
      }

      //  Only consider activating if we are not already activated...
      if (!_activated) {
        //  Don't activate again until the ActivationContext decides all criteria have been met:
        _activated = true;

        //  Let the ActivationContext know it is one variable closer to activating:
        _variableRefCount = _variableRefCount - 1;

        //  Share the good news with whoever is listening.  (Likely only the ActivationContext)...
        for (IListener listener : _changeListeners)
          listener.changed(ref, context);
        return;
      }

      //  If there no PKVariables, then any change to a MeasureVariable should trigger
      //  an activation.  If there are PKVariables, then it's for them to decide...
      if (_pkVariableCount <= 0)
        for (IListener listener : _changeListeners)
          listener.changed(ref, context);
    }

    @Override
    public void added(M ref, IMonadex<? extends IReadOnlyMonad> context) {

    }

    @Override
    public void removed(M ref, IMonadex<? extends IReadOnlyMonad> context) {

    }

    @Override
    public void reverted(M ref, IMonadex<? extends IReadOnlyMonad> context) {

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
  }
}
