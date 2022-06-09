package edu.ahuber1.robot.endeavor

import robocode.TeamRobot

// This extension circumvents the awkward property syntax naming due to robocode being developed in java
inline var TeamRobot.adjustGunForRobotTurn: Boolean
    get() = isAdjustGunForRobotTurn
    set(value) {
        isAdjustGunForRobotTurn = value
    }

// This circumvents the awkward property syntax naming due to robocode being developed in java
inline var TeamRobot.adjustRadarForGunTurn: Boolean
    get() = isAdjustRadarForGunTurn
    set(value) {
        isAdjustRadarForGunTurn = value
    }

