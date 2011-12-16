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
import sun.reflect.generics.tree.ArrayTypeSignature;

import java.util.*;

public abstract class RuleSystemCore<P, R extends IBehavior> implements IBehavior {
  protected R[] _rules = null;
  protected Monadex<IMonad<P>> _intrinsicMemory = new Monadex<IMonad<P>>();
  protected Monadex<IMonad<P>> _alphaMemory = new Monadex<IMonad<P>>();
  protected ArrayDeque<ActivationContext<P, R>> _activationQueue = new ArrayDeque<ActivationContext<P, R>>();

  protected IListener _activator = new IListener() {
    @Override
    public void changed(Object ref, IMonadex context) {
      ActivationContext<P, R> betaMemory = (ActivationContext<P, R>) ref;
      if (betaMemory.isActivatable()) {
        _activationQueue.add(betaMemory);
      }
    }

    @Override
    public void added(Object ref, IMonadex context) {
    }

    @Override
    public void removed(Object ref, IMonadex context) {
    }

    @Override
    public void reverted(Object ref, IMonadex context) {
    }
  };

  public RuleSystemCore(R... rules) {
    _rules = rules;
    _assembleActivationContexts();
  }

  public void clearActivationQueue() {
    _activationQueue.clear();
  }

  @Override
  public IReadWriteMonadex freeVariables() {
    return _alphaMemory;
  }

  @Override
  public String[] freeVarNames() {
    List<String> names = new ArrayList<String>();
    for (IReadOnlyMonad<?> monad : _alphaMemory.getMonads())
      names.add(monad.getName());
    return names.toArray(new String[names.size()]);
  }

  @Override
  public void setFreeVariables(Map<String, IMonad> freeVars) {
    for (IBehavior rule : _rules)
      rule.setFreeVariables(freeVars);
    _assembleActivationContexts();
  }

  public void defineVariable(IMonad<P> variable) {
    _intrinsicMemory.defineVariableBinding(variable);
  }

  protected void _assembleActivationContexts() {
    //  Start assembling alpha memory by drawing on predefined intrinsic monads...
    List<IMonad> alphaList = new ArrayList<IMonad>();
    alphaList.addAll(Arrays.asList(_intrinsicMemory.getMonads()));

    //  Now spin thru the rules and get the alpha from each rule, creating a beta memory for it and merging
    //  the monads from it with the existing list of monads in the alpha memory so that there are no duplicates...
    for (R rule : _rules) {
      //  Create a BetaMemory which will serve as activation context for this rule:
      ActivationContext<P, R> betaMemory = (ActivationContext<P, R>) ((ICARule<P>)rule).getActivationContext();
      for (IMonad<P> cref : betaMemory.monads()) {
        //  Assemble these freeVars into the existing list for the AlphaMemory.  If the AlphaMemory already contains
        //  a CRef for a particular freeVar, add this BetaMemory as a listener to the existing CRef and add a reference
        //  to the existing CRef into the BetaMemory in place of the one returned by getFreeVariables()...
        IMonad<P> ruleCref = _alphaMemory.getMonad(cref.getName());
        if (ruleCref == null) {
          if (!_addToAlphaList(cref, alphaList, betaMemory))
            alphaList.add(cref);
        } else {
          betaMemory.setMonad(ruleCref);
        }
      }
      betaMemory.addChangeListener(_activator);
    }
    _alphaMemory = new Monadex(alphaList.toArray(new IMonad[0]));
  }

  private boolean _addToAlphaList(IMonad cref, List<IMonad> alphaList, ActivationContext<P, R> betaMemory) {
    for (IMonad curCRef : alphaList)
      if (curCRef.getName().equals(cref.getName())) {
        betaMemory.setMonad(curCRef);
        return true;
      }
    return false;
  }
}
