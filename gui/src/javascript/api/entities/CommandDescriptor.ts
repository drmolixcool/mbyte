///
/// CommandDescriptor type used by the GUI to represent manager API command descriptors
///

export type CommandDescriptor = {
  appType: string
  // set of allowed application statuses for which the command is applicable
  appStatus?: string[]
  name: string
  // version is a string on the backend (nullable)
  version?: string | null
  description?: string | null
}
