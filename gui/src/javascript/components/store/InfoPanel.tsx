import { CCard, CCardBody } from '@coreui/react'
import Node from '../../api/entities/Node'

type InfoPanelProps = Readonly<{
  selected: Node | null
}>

export function InfoPanel({ selected }: InfoPanelProps) {
  const formatSize = (size?: number) => {
    if (!size) return ''
    if (size < 1024) return `${size} B`
    if (size < 1024 * 1024) return `${Math.round(size / 1024)} KB`
    return `${Math.round(size / (1024 * 1024))} MB`
  }

  return (
    <div style={{ width: 360, borderLeft: '1px solid rgba(0,0,0,0.08)', overflow: 'auto' }}>
      <div className="p-3">
        <h6>Details</h6>
        {!selected && <div className="text-muted small">Select an item to see details</div>}
        {selected && (
          <CCard>
            <CCardBody>
              <div className="mb-2"><strong>{selected.name}</strong></div>
              <div className="text-muted small">Type: {selected.isFolder ? 'folder' : 'file'}</div>
              <div className="text-muted small">Size: {formatSize(selected.size)}</div>
              <div className="text-muted small">Modified: {selected.modificationTs ? new Date(selected.modificationTs).toLocaleString() : ''}</div>
            </CCardBody>
          </CCard>
        )}
      </div>
    </div>
  )
}
