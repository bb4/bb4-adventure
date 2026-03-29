#!/usr/bin/env python3
"""One-off generator for ludlowScript.xml from structured scene data."""
import xml.sax.saxutils as esc


def scene(name: str, desc: str, choices: list[tuple[str, str]]) -> str:
    d = esc.escape(desc.strip())
    lines = [f'    <scene name="{esc.escape(name)}">', f"        <description>", d, "        </description>"]
    if choices:
        lines.append("        <choices>")
        for ctext, dest in choices:
            lines.append(
                f'            <choice description="{esc.escape(ctext)}" resultScene="{esc.escape(dest)}"/>'
            )
        lines.append("        </choices>")
    lines.append("    </scene>")
    return "\n".join(lines)


def main() -> None:
    scenes: list[str] = []
    # --- Meta / intro ---
    scenes.append(
        scene(
            "start",
            """
***************************************************
********* The Mansion of Professor Ludlow *********
***************************************************
      Adapted from a D&D module by James Ward

You are on a camping trip with a large group of boy scouts.
During a nighttime hike, you get separated from the rest of the group.
You stumble upon a meadow. There in front of you is a large mansion.
You hear an eerie plea for help coming from inside.
What do you do?
""",
            [
                ("Knock on the front door", "no_response"),
                ("Open the front door without knocking", "A.inside_entrance"),
                ("Scream", "sky_falls"),
            ],
        )
    )
    scenes.append(
        scene(
            "no_response",
            """
No one comes to the door and you hear nothing.
A few minutes pass. Still nothing.
""",
            [
                ("Knock again", "no_response"),
                ("Open the front door without knocking", "A.inside_entrance"),
                ("Scream", "sky_falls"),
                ("Return to the rest of your group", "quit"),
            ],
        )
    )
    scenes.append(
        scene(
            "open_crates",
            """
It takes you a really long time, but you finally open and inspect all the crates.
You find nothing in them except for leaves, dust, and stray paperclips.
""",
            [("Continue.", "D.crate_room")],
        )
    )
    scenes.append(
        scene(
            "sky_falls",
            "The sky falls on you. Game over.",
            [("Start over", "start")],
        )
    )
    scenes.append(
        scene(
            "quit",
            """
You go back to the others. They don't believe your story. You enjoy the rest of the camping trip.
""",
            [("Return to the mansion", "start")],
        )
    )

    # A entrance — expanded exits
    scenes.append(
        scene(
            "A.inside_entrance",
            """
The double door is unlocked and opens easily.
The beam of your flashlight reveals an empty hallway with a mirror at the opposite end.
There are openings that lead to rooms on the left and right.
The hall has inch-thick red carpeting and walnut-paneled walls.
The mirror at the end runs from the floor to the twenty-foot-high ceiling and covers the forty feet of wall section on that south face of the hall.
(Referee note: the mirror lifts to reveal a secret door. All first-floor walls are actually unbreakable metal.)
""",
            [
                ("Go through left (east) opening", "B.rat_room"),
                ("Go through right (west) opening", "K.trophyRoom"),
                ("Tamper with mirror at the end of the hall (to the south)", "A.mirror"),
                ("Explore the north wing (areas M–O)", "M.marbleBedroom"),
                ("Enter the special halls (gold / mirror / silver)", "goldHall"),
                ("Exit the mansion", "quit"),
            ],
        )
    )

    # First floor M–O (module first floor)
    scenes.append(
        scene(
            "M.marbleBedroom",
            """
The beams of your flashlights show a room with walls, floor, and ceiling of white marble.
There is a bed in one corner, several dressers, and a floor-to-ceiling mirror on one wall.
The bed is a huge four-poster with dust on the green satin bedspread.
Four dressers hold white operating gowns and masks; bedstands hide dimes, batteries, and a gold letter opener.
(Referee: under the pillow is a red plastic disc—when worn, every room lights up. If anyone lies fully on the bed, the canopy tries to suffocate them for 5 hp/round until cut away.)
""",
            [
                ("Continue to the armor-lined hall", "N.armorHall"),
                ("Return to the entrance hall", "A.inside_entrance"),
            ],
        )
    )
    scenes.append(
        scene(
            "N.armorHall",
            """
The beams of your flashlights show a room with twelve figures dressed in metal.
On the opposite side of the room is another exit.
The room is paneled in red oak. The twelve figures are suits of armor in two rows of six, each with an upraised sword.
(Referee: if the two northernmost suits are touched, they animate and attack. There are two such rooms labeled N on the map—this is one of them.)
""",
            [
                ("Proceed to the kitchen", "O.kitchen"),
                ("Back to the marble bedroom", "M.marbleBedroom"),
            ],
        )
    )
    scenes.append(
        scene(
            "O.kitchen",
            """
The beams of your flashlights show a room with a large table and chairs, cupboards, metal sinks, a gas stove, and a huge refrigerator.
The room is painted yellow. Cupboards hold canned goods; pots and pans fill another cupboard.
(Referee: if the freezer is broken into, a white pudding attacks. Two such kitchen rooms exist on the map.)
""",
            [
                ("Follow the passage toward the trophy wing", "K.trophyRoom"),
                ("Return toward the armor hall", "N.armorHall"),
            ],
        )
    )

    # Special halls chain
    scenes.append(
        scene(
            "goldHall",
            """
The beams of your flashlights reveal a short hall with gold-colored metal walls, floor, and ceiling.
The hall is sixty feet east-west and twenty feet north-south, plated with thin riveted squares.
(Referee: the plates are real gold worth 9 gp each.)
""",
            [
                ("Enter the mirror hall", "mirrorHall"),
                ("Return to the entrance hall", "A.inside_entrance"),
            ],
        )
    )
    scenes.append(
        scene(
            "mirrorHall",
            """
The beams of your flashlights reveal a hall lined with mirrors, 140 feet long east-west.
Light seems to build unnaturally each round you stand here.
(Referee: every 21st melee round, stored light disintegrates the darkest target for 100 damage, then resets.)
""",
            [
                ("Continue to the silver hall", "silverHall"),
                ("Back to the gold hall", "goldHall"),
            ],
        )
    )
    scenes.append(
        scene(
            "silverHall",
            """
The beams of your flashlights reveal a hall with silver-colored metal walls, eighty feet long.
(Referee: plates are solid silver worth 3 gp each.)
""",
            [
                ("Enter the closet rooms", "closets"),
                ("Descend toward the annex pool (area AA)", "AA.pool"),
                ("Back to the mirror hall", "mirrorHall"),
            ],
        )
    )
    scenes.append(
        scene(
            "closets",
            """
Rooms filled with clothing: coats, raincoats, boots—all sized for a tall man.
""",
            [
                ("Continue to the grape-juice pool room", "AA.pool"),
                ("Return to the silver hall", "silverHall"),
            ],
        )
    )

    # AA–EE basement / annex (module)
    scenes.append(
        scene(
            "AA.pool",
            """
The beams of your flashlights reveal a swimming pool filled with dark liquid. A ten-foot concrete border lets you walk around it.
The liquid smells sickeningly sweet.
(Referee: it is grape juice, fresh and drinkable; the pool is thirty feet deep.)
""",
            [
                ("Go to the copper-walled room", "BB.copper"),
                ("Back toward the silver hall", "silverHall"),
            ],
        )
    )
    scenes.append(
        scene(
            "BB.copper",
            """
An empty room whose walls are dull copper-colored metal, forty feet square.
""",
            [
                ("Enter the L-shaped marble room", "CC.corridor"),
                ("Return to the pool room", "AA.pool"),
            ],
        )
    )
    scenes.append(
        scene(
            "CC.corridor",
            """
An L-shaped room: paneled oak walls, grey marble floor and ceiling, wide hallway exit and another opening diagonally opposite.
(Referee: two CC rooms exist; wide openings lead toward the Silver Hall and Gold Hall.)
""",
            [
                ("Descend toward the black spiral room", "DD.blackSpiral"),
                ("Back to the copper room", "BB.copper"),
            ],
        )
    )
    scenes.append(
        scene(
            "DD.blackSpiral",
            """
A room with black barn-board walls and a metal spiral staircase going up at one end.
Eighty feet east-west, forty feet north-south.
""",
            [
                ("Climb the spiral", "EE.sheetRoom"),
                ("Return to the L-shaped room", "CC.corridor"),
            ],
        )
    )
    scenes.append(
        scene(
            "EE.sheetRoom",
            """
A room with several objects covered by sheets—the walls are dark oak beneath foam rubber bits if you dig.
(Referee: a spiral staircase of metal may be uncovered here.)
""",
            [
                ("Return to the entrance hall", "A.inside_entrance"),
                ("Back to the black spiral room", "DD.blackSpiral"),
            ],
        )
    )

    # R S T U first floor (after Q chain)
    scenes.append(
        scene(
            "R.foamRoom",
            """
A room filled top to bottom with small bits of foam rubber. Moving through it takes time.
Underneath, surfaces are dark oak; a metal spiral staircase may lie hidden.
""",
            [
                ("Push on to the computer room", "S.computerRoom"),
                ("Return to the skeleton room", "Q.skeleton_room"),
            ],
        )
    )
    scenes.append(
        scene(
            "S.computerRoom",
            """
A huge computer fills both sides of the room; a narrow path runs between the machines.
On the east side, a table holds a typewriter; above it, a television screen.
(Referee: the computer answers typed questions about the house; technical answers come back in Latin.)
""",
            [
                ("Continue to the pink fiberglass bathroom", "T.pinkBathroom"),
                ("Back to the foam room", "R.foamRoom"),
            ],
        )
    )
    scenes.append(
        scene(
            "T.pinkBathroom",
            """
A bathroom with stool, sink, mirror, and tub. Everything is pink fiberglass; the tub bubbles.
(Referee: the tub holds hydrochloric acid—dissolves anything except glass. The medicine cabinet behind the mirror is empty.)
""",
            [
                ("Go to the fungus-filled library", "U.fungusLibrary"),
                ("Back to the computer room", "S.computerRoom"),
            ],
        )
    )
    scenes.append(
        scene(
            "U.fungusLibrary",
            """
A room filled with books on shelves, a desk, and another exit. Fungus coats surfaces five inches thick; spores cloud the air.
(Referee: harmless but foul-smelling; the pile hides +4 glowing plate mail.)
""",
            [
                ("Return to the entrance hall", "A.inside_entrance"),
                ("Back to the pink bathroom", "T.pinkBathroom"),
            ],
        )
    )

    # --- Original B through Q, F, etc. (paste key scenes) ---
    scenes.append(
        scene(
            "B.rat_room",
            """
The beams of your flashlight show a room paneled in red-stained barn boards with a floor of the same substance.
There is a couch in the northeast corner and an opening on the east wall.
Leaves litter the floor; the couch is dusty and overstuffed.
The room measures sixty feet east and west and forty feet north and south.
""",
            [
                ("Inspect the couch.", "B.rat_attack"),
                ("Sift through the leaves on the floor.", "B.key"),
                ("Go through the door on the east side.", "C.pink_hallway"),
                ("Go back to the inside entrance", "A.inside_entrance"),
            ],
        )
    )
    scenes.append(
        scene(
            "C.pink_hallway",
            """
The beam of your flashlight shows a hall with pink walls and a floor of red marble.
Clean squares mark where pictures once hung. The hall is eighty feet east-west, twenty feet north-south.
""",
            [
                ("Proceed into the room at the end of the hall (east).", "D.crate_room"),
                ("Back to previous room (west)", "B.rat_room"),
            ],
        )
    )
    scenes.append(
        scene(
            "D.crate_room",
            """
Gray painted walls, cement floor, wooden crates, leaves in piles.
Two hundred empty crates; the biggest leaf piles are in the southwest alcove.
Behind a big crate on the east wall, thirty feet from the north corner, is a door with a normal handle.
""",
            [
                ("Go through door behind big crate (east).", "E.gray_storage_room"),
                ("Look inside all the crates.", "open_crates"),
                ("Sift through the pile of leaves on the floor.", "D.crate_room_leaves"),
                ("Back to hallway (west)", "C.pink_hallway"),
            ],
        )
    )
    scenes.append(
        scene(
            "D.crate_room_leaves",
            """
There are snakes in the leaves. Three four-foot rattlesnakes attack.
(HP: 10,8,5; #AT: 1; damage:1-3; AC: 5; SA: Save versus death caused by poison).
""",
            [("Continue.", "D.crate_room")],
        )
    )
    scenes.append(
        scene(
            "F.oak_room",
            """
Oak-paneled room filled with rock shelves; stairs go up from the south.
A partial wall divides the space; passageway on the west in the north corner; doorway farther south; another exit by the stairs on the east.
""",
            [
                ("Head south and climb the stairs.", "F.stairway"),
                ("Exit to the east using the opening at the base of the stairs.", "J.Bathroom"),
                ("Inspect the rocks on the shelves.", "F.rocks"),
                ("Exit to the west into the passageway at the north corner", "G.passageway"),
                ("Go back to gray storage room.", "E.gray_storage_room"),
            ],
        )
    )
    scenes.append(
        scene(
            "E.crazy_man_attacks",
            """
An incredibly old man in rags attacks with a butcher knife
(HP: 7; #AT: 1; damage: 1-6; AC: 10).
He will not follow you out of the room.
""",
            [
                ("Retreat to the crate room", "D.crate_room"),
                ("If you survive or subdue him, continue searching", "E.gray_storage_room"),
            ],
        )
    )
    scenes.append(
        scene(
            "E.gray_storage_room",
            """
Gray walls, cement floor, boxes and barrels: grain alcohol, gloves, silver service, plant books, clay pots, mattress crates hiding the east door.
""",
            [
                ("Go through the door on the east side.", "F.oak_room"),
                ("Inspect boxes and barrels further.", "E.crazy_man_attacks"),
                ("Go back to the room with the crates (west).", "D.crate_room"),
            ],
        )
    )
    scenes.append(
        scene(
            "G.passageway",
            """
Gray walls, black marble floor, openings east and west. Medieval weapons hang in perfect condition.
The hall is twenty feet by one hundred feet.
""",
            [
                ("Travel west along the hallway to the door at the end.", "H.fur_room"),
                ("Go back to the oak storage room.", "F.oak_room"),
            ],
        )
    )
    scenes.append(
        scene(
            "F.stairway",
            """
A creaky wooden stairway climbs from the south end of the oak-walled rock gallery.
Cold air drifts from above; a landing and closed door wait at the top.
""",
            [
                ("Climb the stairs to the second floor", "VII.chunksRoom"),
                ("Go back down into the oak room", "F.oak_room"),
            ],
        )
    )
    scenes.append(
        scene(
            "F.rocks",
            """
Valuable mineral samples sit on high shelves on the south wall in the corridor behind the dividing wall—no crystal samples anywhere.
""",
            [("Return to the oak room", "F.oak_room")],
        )
    )
    scenes.append(
        scene(
            "H.fur_room",
            """
Purple furs cover walls, ceiling, and floor. A metal spiral staircase in the northwest corner is painted purple enamel.
Eighty by sixty feet.
""",
            [
                ("Inspect furs on wall.", "H.inspect_wall"),
                ("Go up the spiral staircase.", "H.spiral_staircase"),
                ("Go back to the passageway.", "G.passageway"),
            ],
        )
    )
    scenes.append(
        scene(
            "H.inspect_wall",
            """
Tapping shows a hollow; a section of fur slides aside to reveal a normal door.
""",
            [
                ("Go through the secret door.", "I.statue_room"),
                ("Stay in fur room.", "H.fur_room"),
            ],
        )
    )
    scenes.append(
        scene(
            "H.spiral_staircase",
            """
The purple metal spiral is steep. At the top, air grows warm and furred—you emerge in another fur-lined chamber.
""",
            [
                ("Explore the fur-lined room at the top", "X.spiralStaircase"),
                ("Climb back down", "H.fur_room"),
            ],
        )
    )
    scenes.append(
        scene(
            "I.statue_room",
            """
Green wood paneling; fifteen statues—Vikings, amazons, knights, and five Wisconsin State Patrol figures with nightsticks.
Sixty by forty feet.
(Referee: all are petrified people, aware but unable to communicate.)
""",
            [
                ("Attempt to speak with the statues", "I.talk_to_statues"),
                ("Attack the statues", "I.attack_statues"),
                ("Slip back through the secret door", "H.fur_room"),
            ],
        )
    )
    scenes.append(
        scene(
            "I.talk_to_statues",
            "They make grunting noises back to you.",
            [
                ("Attack the statues.", "I.attack_statues"),
                ("Continue", "I.statue_room"),
            ],
        )
    )
    scenes.append(
        scene(
            "I.attack_statues",
            """
Your blows clang on stone. The module notes these are living people, petrified—unable to respond.
""",
            [("Stop and look for another way to help them", "I.statue_room")],
        )
    )
    scenes.append(
        scene(
            "A.mirror",
            "You find that the mirror lifts easily. There is a secret door behind it.",
            [
                ("Open the secret door", "P.plant_room"),
                ("Lower the mirror and return", "A.inside_entrance"),
            ],
        )
    )
    scenes.append(
        scene(
            "P.plant_room",
            """
Plants in glowing purple liquid fill tanks wall to wall; the path across is about eighty feet.
Thorny brambles block the secret door until cut—cutting risks stirges.
""",
            [
                ("Inspect or cut the plants (risk attacks)", "P.inspect_plants"),
                ("Pick your way carefully toward the far exit", "Q.skeleton_room"),
                ("Return to the entrance hall", "A.inside_entrance"),
            ],
        )
    )
    scenes.append(
        scene(
            "P.inspect_plants",
            """
Seven stirges burst from the foliage when it is disturbed.
(HP 5 each; #AT 1; damage 1-3; AC 8; blood drain.)
""",
            [("Fight or flee back toward the secret door", "P.plant_room")],
        )
    )
    scenes.append(
        scene(
            "J.Bathroom",
            """
Black marble bathroom: sink, mirror, stool, tub. Water shuts off before overflow. Hinges suggest a missing door.
Forty-foot square plus a tub hall.
""",
            [
                ("Examine the sink and turn on the water", "J.turnOnWater"),
                ("Open the medicine cabinet", "J.medicine_cabinet"),
                ("Return to the oak rock gallery", "F.oak_room"),
            ],
        )
    )
    scenes.append(
        scene(
            "J.medicine_cabinet",
            """
Two plastic bottles: ten green pills; twenty pink pills.
""",
            [
                ("Swallow one of the green pills", "J.takeGreenPill"),
                ("Swallow one of the pink pills", "J.takePinkPill"),
                ("Close the cabinet", "J.Bathroom"),
            ],
        )
    )
    scenes.append(
        scene(
            "B.rat_attack",
            """
Three giant rats boil out from under the couch.
(HP 4,3,2; #AT 1; damage 1-3; AC 7; 5% disease on bite.)
""",
            [("Back away from the couch", "B.rat_room")],
        )
    )
    scenes.append(
        scene(
            "B.key",
            """
You find a golden key under the pile of leaves.
(Keep an inventory of found items in play.)
""",
            [("Continue", "B.rat_room")],
        )
    )
    scenes.append(
        scene(
            "L.upStaircase",
            """
Rat fur covers walls, floor, and ceiling; lice and rot smell. A black-enameled metal spiral rises in the northwest corner.
Sixty feet square beneath the fur.
""",
            [
                ("Walk up the spiral staircase.", "X.spiralStaircase"),
                ("Return to the trophy room", "K.trophyRoom"),
            ],
        )
    )
    scenes.append(
        scene(
            "K.trophyRoom",
            """
Stuffed felines on brown walls; rifles and shotguns in a north-wall cabinet with shells in the drawer.
Forty by sixty feet. Exits east and west.
""",
            [
                ("Examine the rifles (risk mishandling)", "K.fireRifle"),
                ("Proceed west toward the rat-fur spiral room", "L.upStaircase"),
                ("Return to the entrance hall (east)", "A.inside_entrance"),
            ],
        )
    )
    scenes.append(
        scene(
            "K.fireRifle",
            """
You heft a heavy rifle. Without a gunnery merit badge, working the levers is guesswork—metal slams into your shoulder (1–4 bruising) and the shot goes wide.
The trophies seem to watch in the dim light.
""",
            [("Set the rifle down", "K.trophyRoom")],
        )
    )
    scenes.append(
        scene(
            "X.spiralStaircase",
            """
Thick furs—zebra, lion, leopard, polar bear—worth serious treasure. A side exit leads off the landing.
Four giant centipedes may appear when several people step on the furs.
""",
            [
                ("Descend toward the rat-fur room", "L.upStaircase"),
                ("Descend toward the purple fur chamber", "H.fur_room"),
            ],
        )
    )

    # Second floor VII + chain
    scenes.append(
        scene(
            "VII.chunksRoom",
            """
Metal chunks litter the floor; three fake oak doors refuse to open. Chunks rain from the ceiling each round after entry.
(Referee: 3–18 chunks per round, 15% hit chance each, 1–6 damage.)
""",
            [
                ("Search for another exit among the scrap", "Floor2.I"),
                ("Retreat down the stairs to the oak room", "F.stairway"),
            ],
        )
    )

    floor2 = [
        (
            "Floor2.I",
            "Yellow carpet; huge spiderwebs on the ceiling. Thirty feet square; stairs ascend along the west wall. Three giant spiders wait above.",
            "Floor2.II",
        ),
        (
            "Floor2.II",
            "Nine benches of glassware, acids, and green flammable jelly. Another exit hides a spiral staircase down.",
            "Floor2.III",
        ),
        (
            "Floor2.III",
            "A barbed devil is chained in a silver circle, pleading in foreign tongues. Boxes underfoot hold a ring of one wish and dust of disappearance.",
            "Floor2.IV",
        ),
        (
            "Floor2.IV",
            "Bare room: dazzling lights and deafening sound when active. Sunburn and hearing loss if you linger.",
            "Floor2.V",
        ),
        (
            "Floor2.V",
            "A Persian rug hides a central pit. Stay near the walls or fall fifteen feet.",
            "Floor2.VI",
        ),
        (
            "Floor2.VI",
            "Super-magnet in the ceiling seizes metal; a glass plate on the floor covers the off switch.",
            "Floor2.VIII",
        ),
        (
            "Floor2.VIII",
            "Bare room, stairs down on a south landing; flower-scented gas weakens Strength. Secret door on the landing opens if light shines one minute.",
            "Floor2.IX",
        ),
        (
            "Floor2.IX",
            "Electronics lab: chips, tubes, solder, barrels of wire. Ascending stairs in the east corner lead up.",
            "Floor2.X",
        ),
        (
            "Floor2.X",
            "Bunk beds, dressers, desk strewn with cloning notes; secret drawer holds poison gas and a photo of Ludlow with a giant robot.",
            "Floor2.XI",
        ),
        (
            "Floor2.XI",
            "Shooting gallery with air rifles, pellets, and targets—disturbing the guns may release a lion.",
            "Floor2.XII",
        ),
        (
            "Floor2.XII",
            "Satin cushions; a winged woman sleeps—an Erinyes who feigns kindness if freed.",
            "Floor2.XIII",
        ),
        (
            "Floor2.XIII",
            "Strong purple light; intense heat heals damage but burns if you stay past ten minutes.",
            "Floor2.XIV",
        ),
        (
            "Floor2.XIV",
            "A hill giant shambles in a cage; rags hide cash, a laser pistol, and a bloody knife.",
            "Floor2.XV",
        ),
        (
            "Floor2.XV",
            "Locked cabinets of pure elements and chemicals—dangerous if mishandled.",
            "Floor2.XVI",
        ),
        (
            "Floor2.XVI",
            "Shower room with ten stalls; water drains to a central grate.",
            "Floor2.XVII",
        ),
        (
            "Floor2.XVII",
            "Pipe storage and threading tools—enough stock to improvise clubs.",
            "Floor2.XXI",
        ),
        (
            "Floor2.XXI",
            "Torture devices: rack, iron maiden, branding irons with 'L'. Good campers should find it revolting.",
            "Floor2.XXII",
        ),
        (
            "Floor2.XXII",
            "Bright theater: projector, screen, film canister 'Laser Rifle Care by Ludlow'—ropers drop from the ceiling mid-film.",
            "Floor2.XXIII",
        ),
        (
            "Floor2.XXIII",
            "Assembly benches for laser-rifle parts; a completed prototype may lie under Bench Eight.",
            "Floor3.X",
        ),
    ]

    for i, (name, desc, nxt) in enumerate(floor2):
        prev = "VII.chunksRoom" if i == 0 else floor2[i - 1][0]
        choices = [(f"Press on to the next chamber", nxt), (f"Go back", prev)]
        if name == "Floor2.XXIII":
            choices = [
                ("Ascend to the third floor", nxt),
                ("Return toward the electronics lab", "Floor2.IX"),
            ]
        scenes.append(scene(name, desc, choices))

    # Third floor
    scenes.append(
        scene(
            "Floor3.X",
            """
Glowing pillar ten feet wide; something moves inside the light.
(Referee: breaking the beam frees a hell hound that pursues you.)
""",
            [
                ("Continue", "Floor3.XI"),
                ("Retreat to the assembly room", "Floor2.XXIII"),
            ],
        )
    )
    scenes.append(
        scene(
            "Floor3.XI",
            """
Spiral staircase; a leather sack of ball bearings hovers magnetically in the center.
""",
            [
                ("Continue", "Floor3.XII"),
                ("Back", "Floor3.X"),
            ],
        )
    )
    scenes.append(
        scene(
            "Floor3.XII",
            """
Microscope lab: twenty-two benches of slides and specimens.
""",
            [
                ("Continue", "Floor3.XIII"),
                ("Back", "Floor3.XI"),
            ],
        )
    )
    scenes.append(
        scene(
            "Floor3.XIII",
            """
Purple glow; dried bones in a corner; ultraviolet heat sears if you linger.
""",
            [
                ("Continue", "Floor3.XIV"),
                ("Back", "Floor3.XII"),
            ],
        )
    )
    scenes.append(
        scene(
            "Floor3.XIV",
            """
Robot parts and tentacles on benches; a dormant robot waits in the corner.
(Referee: touching its left shoulder with metal activates it.)
""",
            [
                ("Continue", "Floor3.XV"),
                ("Back", "Floor3.XIII"),
            ],
        )
    )
    scenes.append(
        scene(
            "Floor3.XV",
            """
Everything is coated in red plastic that sticks to your feet every tenth step until you sweat free.
""",
            [
                ("Confront the source of the recording", "storyFinale"),
                ("Back", "Floor3.XIV"),
            ],
        )
    )

    scenes.append(
        scene(
            "storyFinale",
            """
Deep in the mansion you find Professor Ludlow's control nook: a tape recorder on loop, wired to the front doors—there never was a prisoner, only bait.
You smash the device, gather your nerve, and run for the exit before the house can answer.
The night air never tasted so sweet.
(Referee: the 'Help!' was a recording, as noted in the module.)
""",
            [
                ("Leave the mansion behind", "quit"),
                ("Play again from the start", "start"),
            ],
        )
    )

    scenes.append(
        scene(
            "J.turnOnWater",
            "The water runs clear and shuts off before overflow.",
            [("Step back", "J.Bathroom")],
        )
    )
    scenes.append(
        scene(
            "J.takeGreenPill",
            "Your heart hammers—reflexes doubled for three melee rounds (module).",
            [("Put the bottle back", "J.medicine_cabinet")],
        )
    )
    scenes.append(
        scene(
            "J.takePinkPill",
            "Warmth spreads; up to ten hit points restored per pink pill.",
            [("Put the bottle back", "J.medicine_cabinet")],
        )
    )
    scenes.append(
        scene(
            "Q.skeleton_room",
            """
Stone walls; a child's skeleton lies in pieces; a golden ring glints on a finger.
(Referee: skull near body animates the skeleton; ring holds three wishes after the skeleton is destroyed.)
""",
            [
                ("Leave through the plant gallery", "P.plant_room"),
                ("Follow the farther exit toward foam and machinery", "R.foamRoom"),
            ],
        )
    )

    header = """<?xml version="1.0" encoding="UTF-8"?>
<script author="Barry Becker" date="10/19/2006" name="ludlow" title="The Mansion of Professor Ludlow">
"""
    footer = "</script>\n"
    out = header + "\n".join(scenes) + "\n" + footer
    path = "/Users/barry/projects/bb4/bb4-adventure/scala-source/com/barrybecker4/puzzle/adventure/stories/ludlow/ludlowScript.xml"
    with open(path, "w", encoding="utf-8") as f:
        f.write(out)
    print("Wrote", path, "scenes:", len(scenes))


if __name__ == "__main__":
    main()
