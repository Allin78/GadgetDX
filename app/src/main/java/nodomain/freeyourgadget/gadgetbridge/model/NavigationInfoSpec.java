/*  Copyright (C) 2021-2024 Andreas Shimokawa, Arjan Schrijver, Gordon Williams

    This file is part of Gadgetbridge.

    Gadgetbridge is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as published
    by the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Gadgetbridge is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>. */
package nodomain.freeyourgadget.gadgetbridge.model;

public class NavigationInfoSpec {
    public enum Action {
        UNKNOWN(-1),
        CONTINUE(0),
        DEPART(1),
        TURN_LEFT(2),
        TURN_LEFT_SLIGHTLY(3),
        TURN_LEFT_SHARPLY(4),
        TURN_RIGHT(5),
        TURN_RIGHT_SLIGHTLY(6),
        TURN_RIGHT_SHARPLY(7),
        KEEP_LEFT(8),
        KEEP_RIGHT(9),
        UTURN_LEFT(10),
        UTURN_RIGHT(11),
        ROUNDABOUT_RIGHT(12),
        ROUNDABOUT_LEFT(13),
        ROUNDABOUT_STRAIGHT(14),
        ROUNDABOUT_UTURN(15),
        MERGE(16),
        FINISH(17),
        FINISH_LEFT(18),
        FINISH_RIGHT(19),
        OFFROUTE(20);

        public final int id;

        Action(int id) {
            this.id = id;
        }

        // Since the ids of the actions are serialized as ints, this allows conversion back to the enum.
        // Not the most efficient way, but there are only ~20 items to look at so it should be fine.
        public static Action fromId(int id) {
            for (Action type : values()) {
                if (type.id == id) {
                    return type;
                }
            }
            return Action.UNKNOWN;
        }
    }


    // Usually the road name and which action to take.
    public String instruction;
    // Distance to turn (as a string, eg "100m")
    public String distanceToTurn;
    // One of the Action constants.
    public Action nextAction;
    // Estimated time of Arrival.
    public String ETA;
}
