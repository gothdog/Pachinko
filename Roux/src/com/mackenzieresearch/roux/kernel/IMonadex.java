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

package com.mackenzieresearch.roux.kernel;

import java.util.Map;

public interface IMonadex<M extends IReadOnlyMonad<?>> {
  /**
   * Return the value of the variable within the current context whose name is
   * <name>.
   *
   */
  public Object bindValue(String name);

  /**
   * Return the value of the variable within the current context located at
   * integer offset <index>.
   *
   */
  public Object bindValue(int index);

  /**
   * Return the integer offset within the current context of the variable by the
   * name <name>.
   *
   */
  public int getIndex(String name);

  /**
   * Return a context reference to the variable in the current context whose
   * name is <name>. This context reference can be used to subsequently access
   * the value of this variable.
   *
   */
  public M getMonad(String name);

  /**
   * Return all the context references contained in this context.
   *
   */
  public Iterable<? extends M> monads();

  /**
   * Verifies whether a context contains a CRef by this name.
   */
  public boolean containsMonad(String name);

  /**
   * Return a context reference to the variable within the current context
   * found at offset <index>.
   */
  public M getMonad(int index);

   /**
   * Return a list of all the context references contained in this context.
   */

  public M[] getMonads();

  /**
   * Get a map of CRefs...
   */
  public Map<String, M> getVariables();

  /**
   * Return the number of CRefs in this Monad.
   */
  public int size();
}
