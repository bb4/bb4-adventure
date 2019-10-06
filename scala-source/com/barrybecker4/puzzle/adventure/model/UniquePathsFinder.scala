package com.barrybecker4.puzzle.adventure.model

import scala.collection.immutable.Queue

/**
  * Find all possible unique paths (without cycles) from
  * start to destination using an algorithm a little like A* but without
  * the cost heuristic.
  * @param story the story to examine
  */
case class UniquePathsFinder(story: Story) {

  case class Path(visited: Set[Scene], pathList: Seq[Scene]) {

    def this(startScene: Scene) {
      this(Set(startScene), Seq[Scene](startScene))
    }

    def getLast: Scene = pathList.last
    def getNextScenes: Seq[Scene] = story.getSceneMap.getChildScenes(getLast)
    def asSceneList: Seq[Scene] = pathList
    def contains(scene: Scene): Boolean = visited.contains(scene)
    def add(scene: Scene): Path = Path(visited + scene, pathList :+ scene)
  }

  val startScene: Scene = story.getFirstScene

  /** @param destScene the scene to find paths to
    */
  def findUniquePaths(destScene: Scene): List[Seq[Scene]] = {
    var queue = Queue[Path]()
    var uniquePaths = List[Path]()

    queue = queue.enqueue(new Path(startScene))

    while (queue.nonEmpty) {
      val (path, q) = queue.dequeue
      queue = q
      for (child <- path.getNextScenes) {
        if (child == destScene) {
          uniquePaths = uniquePaths :+ path.add(child)
        }
        else if (!path.contains(child)) {
          queue = queue.enqueue(path.add(child))
        }
      }
    }

    //println("found " + uniquePaths.size + " paths")
    uniquePaths.map(_.asSceneList)
  }
}
