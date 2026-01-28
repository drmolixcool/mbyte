///
/// Copyright (C) 2025 Jerome Blanchard <jayblanc@gmail.com>
///
/// This program is free software: you can redistribute it and/or modify
/// it under the terms of the GNU General Public License as published by
/// the Free Software Foundation, either version 3 of the License, or
/// (at your option) any later version.
///
/// This program is distributed in the hope that it will be useful,
/// but WITHOUT ANY WARRANTY; without even the implied warranty of
/// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
/// GNU General Public License for more details.
///
/// You should have received a copy of the GNU General Public License
/// along with this program.  If not, see <https://www.gnu.org/licenses/>.
///

// Frontend representation of a store Node (renamed to Node)
// Mirrors the NodeDto shape and adds convenience helpers.

// Internal DTO type (NOT exported) to avoid exposing NodeDto separately
type NodeData = {
  root?: boolean
  type?: string
  id: string
  parent?: string | null
  name?: string
  mimetype?: string | null
  size?: number
  creation?: string | null
  modification?: string | null
}

export default class Node {
  readonly dto: NodeData

  constructor(dto: NodeData) {
    this.dto = dto
  }

  static fromDto(dto: NodeData): Node {
    return new Node(dto)
  }

  get id(): string {
    return this.dto.id
  }

  get name(): string {
    return this.dto.name ?? ''
  }

  get mimetype(): string | undefined {
    return this.dto.mimetype ?? undefined
  }

  get size(): number | undefined {
    return this.dto.size
  }

  get creationTs(): number | undefined {
    if (!this.dto.creation) return undefined
    const t = Date.parse(this.dto.creation)
    return Number.isNaN(t) ? undefined : t
  }

  get modificationTs(): number | undefined {
    if (!this.dto.modification) return undefined
    const t = Date.parse(this.dto.modification)
    return Number.isNaN(t) ? undefined : t
  }

  get isRoot(): boolean {
    return Boolean(this.dto.root)
  }

  get isFolder(): boolean {
    return (this.dto.type ?? '').toUpperCase() === 'TREE'
  }

  get isFile(): boolean {
    return (this.dto.type ?? '').toUpperCase() === 'BLOB'
  }
}
