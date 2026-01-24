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

import { useCallback, useEffect, useRef, useState } from 'react'
import type { Process } from '../api/entities/Process'
import { useManagerApi } from '../api/ManagerApiProvider'

export type CommandProcessingPhase = 'idle' | 'running' | 'polling' | 'completed' | 'error'

export type UseAppCommandResult = {
  phase: CommandProcessingPhase
  procId: string | null
  currentProcess: Process | null
  error: string | null

  runCommand: (appId: string, command: string) => Promise<void>
  refresh: () => Promise<void>
}

/**
 * App command processing helper:
 * - Run a command (POST /api/apps/{id}/procs)
 * - Poll the process until completion
 */
export function useAppCommandProcessing(appId: string): UseAppCommandResult {
  const managerApi = useManagerApi()
  const managerApiRef = useRef(managerApi)
  useEffect(() => { managerApiRef.current = managerApi }, [managerApi])

  const [phase, setPhase] = useState<CommandProcessingPhase>('idle')
  const [procId, setProcId] = useState<string | null>(null)
  const [currentProcess, setCurrentProcess] = useState<Process | null>(null)
  const [error, setError] = useState<string | null>(null)

  const pollTimer = useRef<ReturnType<typeof setTimeout> | null>(null)

  const stopPolling = useCallback(() => {
    if (pollTimer.current != null) {
      globalThis.clearTimeout(pollTimer.current as unknown as number)
      pollTimer.current = null
    }
  }, [])

  const pollProcess = useCallback(async (procIdArg?: string) => {
    const id = procIdArg ?? procId
    if (!id) return
    try {
      const proc = await managerApiRef.current.getAppProc(appId, id)
      // debug log to help trace status transitions
      // eslint-disable-next-line no-console
      console.debug('pollProcess:', { appId, procId: id, status: proc?.status })
      setCurrentProcess(proc)

      // Continue polling while the process is not in a terminal state
      const terminalStates = ['COMPLETED', 'FAILED', 'ROLLED_BACK']
      if (proc.status && !terminalStates.includes(proc.status)) {
        setPhase('polling')
        // eslint-disable-next-line no-console
        console.debug('pollProcess: scheduling next poll for', id)
        pollTimer.current = globalThis.setTimeout(() => {
          void pollProcess(id)
        }, 2000)
      } else {
        // terminal state reached; attempt one extra fetch to get the final state or detect removal
        try {
          const finalProc = await managerApiRef.current.getAppProc(appId, id)
          // eslint-disable-next-line no-console
          console.debug('pollProcess: final fetch result', { appId, procId: id, status: finalProc?.status })
          setCurrentProcess(finalProc)
        } catch (e: unknown) {
          if (e instanceof Error && e.message.startsWith('HTTP 404')) {
            // process removed, clear currentProcess
            // eslint-disable-next-line no-console
            console.debug('pollProcess: final fetch returned 404, clearing currentProcess for', id)
            setCurrentProcess(null)
          } else {
            // keep the previously fetched proc if final fetch failed with other errors
            // eslint-disable-next-line no-console
            console.debug('pollProcess: final fetch failed for', id, e)
          }
        }
        setPhase('completed')
        // eslint-disable-next-line no-console
        console.debug('pollProcess: terminal status reached for', id, proc.status)
        stopPolling()
      }
    } catch (e: unknown) {
      // If backend removed the process (e.g. 404), consider it completed and stop polling
      if (e instanceof Error && e.message.startsWith('HTTP 404')) {
        // eslint-disable-next-line no-console
        console.debug('pollProcess: process not found, treating as completed', id)
        setPhase('completed')
        setCurrentProcess(null)
        stopPolling()
        return
      }
      // eslint-disable-next-line no-console
      console.debug('pollProcess: error for', id, e)
      setPhase('error')
      setError(e instanceof Error ? e.message : String(e))
      stopPolling()
    }
  }, [procId, appId, stopPolling])

  // On mount, check for active processes
  useEffect(() => {
    if (!appId) return
    let cancelled = false
    ;(async () => {
      try {
        const processes = await managerApiRef.current.getAppProcs(appId, true)
        if (cancelled) return
        if (processes.length > 0) {
          const activeProc = processes[0] // assume one active at a time
          setProcId(activeProc.id)
          setCurrentProcess(activeProc)
          // If the active process is not in a terminal state, start polling
          const terminalStates = ['COMPLETED', 'FAILED', 'ROLLED_BACK']
          if (activeProc.status && !terminalStates.includes(activeProc.status)) {
            setPhase('polling')
            await pollProcess(activeProc.id)
          } else {
            setPhase('completed')
          }
        }
      } catch (e: unknown) {
        // log debug instead of silently ignoring to make the failure visible for debugging
        // but don't surface the error to UI on initial load
        // eslint-disable-next-line no-console
        console.debug('useAppCommandProcessing: initial load error', e)
      }
    })()
    return () => {
      cancelled = true
      stopPolling()
    }
  }, [appId, pollProcess, stopPolling])

  const runCommand = useCallback(async (appId: string, command: string) => {
    try {
      setError(null)
      setPhase('running')
      const pid = await managerApiRef.current.runAppCommand(appId, command)
      setProcId(pid)
      setPhase('polling')
      await pollProcess(pid)
    } catch (e: unknown) {
      setPhase('error')
      setError(e instanceof Error ? e.message : String(e))
    }
  }, [pollProcess])

  const refresh = useCallback(async () => {
    if (procId) {
      await pollProcess(procId)
    }
  }, [pollProcess, procId])

  return { phase, procId, currentProcess, error, runCommand, refresh }
}
