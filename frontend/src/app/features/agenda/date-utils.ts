/** Utilidades mínimas de fechas para la agenda de papel (agrupar tareas por día, pasar de página). */

export function dateKey(date: Date): string {
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`;
}

export function parseDateKey(key: string): Date {
  const [year, month, day] = key.split('-').map(Number);
  return new Date(year, month - 1, day);
}

export function startOfDay(date: Date): Date {
  const result = new Date(date);
  result.setHours(0, 0, 0, 0);
  return result;
}

export function addDays(date: Date, amount: number): Date {
  const result = new Date(date);
  result.setDate(result.getDate() + amount);
  return result;
}

/** Índice de día de semana con lunes=0..domingo=6 (Date.getDay() usa domingo=0). */
export function mondayFirstWeekday(date: Date): number {
  return (date.getDay() + 6) % 7;
}

export interface MonthGrid {
  /** Primer día del mes, usado como etiqueta (nombre de mes) y para comparar el mes activo. */
  date: Date;
  /** Semanas de 7 casillas (lunes a domingo); `null` son huecos antes/después del mes. */
  weeks: (Date | null)[][];
}

/** Construye la rejilla de un mes (tipo calendario de pared) para el carril de meses de la agenda. */
export function buildMonthGrid(year: number, monthIndex: number): MonthGrid {
  const first = new Date(year, monthIndex, 1);
  const totalDays = new Date(year, monthIndex + 1, 0).getDate();
  const leadingBlanks = mondayFirstWeekday(first);

  const cells: (Date | null)[] = new Array(leadingBlanks).fill(null);
  for (let day = 1; day <= totalDays; day += 1) {
    cells.push(new Date(year, monthIndex, day));
  }
  while (cells.length % 7 !== 0) {
    cells.push(null);
  }

  const weeks: (Date | null)[][] = [];
  for (let i = 0; i < cells.length; i += 7) {
    weeks.push(cells.slice(i, i + 7));
  }

  return { date: first, weeks };
}

const WORKSPACE_TAB_COLORS = ['var(--tab-1)', 'var(--tab-2)', 'var(--tab-3)', 'var(--tab-4)'];

/** Asigna un color de "pestaña" determinista a un espacio de trabajo, coherente con el resto de la app. */
export function workspaceTabColor(name: string): string {
  let hash = 0;
  for (let i = 0; i < name.length; i += 1) {
    hash = (hash * 31 + name.charCodeAt(i)) >>> 0;
  }
  return WORKSPACE_TAB_COLORS[hash % WORKSPACE_TAB_COLORS.length];
}
