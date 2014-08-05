import AssemblyKeys._

assemblySettings

val meta = """META.INF(.)*""".r

mergeStrategy in assembly <<= (mergeStrategy in assembly) { (old) =>
  {
     case meta(_) => MergeStrategy.discard
     case x => MergeStrategy.first
  }
}
