import { Component, signal, HostListener } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';

@Component({
  selector: 'app-shell',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  template: `
    <div class="shell">
      <!-- Desktop sidebar -->
      <aside class="sidebar">
        <div class="sidebar__brand">
          <span class="text-accent" style="font-size:0.65rem;font-weight:700;letter-spacing:0.1em">FITNESS AI COACH</span>
        </div>
        <nav class="sidebar__nav">
          <a routerLink="/dashboard" routerLinkActive="active" class="nav-item">Panel</a>
          <a routerLink="/chat"      routerLinkActive="active" class="nav-item">Chat</a>
          <a routerLink="/profile"   routerLinkActive="active" class="nav-item">Perfil</a>
        </nav>
      </aside>

      <!-- Mobile top bar -->
      <header class="topbar">
        <span class="topbar__brand">FITNESS AI COACH</span>
        <button class="hamburger" (click)="toggleMenu()" [attr.aria-expanded]="menuOpen()" aria-label="Menú">
          <span class="bar" [class.open]="menuOpen()"></span>
          <span class="bar" [class.open]="menuOpen()"></span>
          <span class="bar" [class.open]="menuOpen()"></span>
        </button>
      </header>

      <!-- Mobile drawer overlay -->
      @if (menuOpen()) {
        <div class="overlay" (click)="closeMenu()"></div>
        <nav class="drawer" [class.open]="menuOpen()">
          <div class="drawer__brand">FITNESS AI COACH</div>
          <a routerLink="/dashboard" routerLinkActive="active" class="drawer-item" (click)="closeMenu()">Panel</a>
          <a routerLink="/chat"      routerLinkActive="active" class="drawer-item" (click)="closeMenu()">Chat</a>
          <a routerLink="/profile"   routerLinkActive="active" class="drawer-item" (click)="closeMenu()">Perfil</a>
        </nav>
      }

      <main class="content">
        <router-outlet />
      </main>
    </div>
  `,
  styles: [`
    .shell {
      display: flex;
      height: 100vh;
      overflow: hidden;
    }

    /* ── Desktop sidebar ── */
    .sidebar {
      width: 160px;
      min-width: 160px;
      background: var(--surface);
      border-right: 1px solid var(--border);
      display: flex;
      flex-direction: column;
      padding: 1.5rem 0;
    }
    .sidebar__brand {
      padding: 0 1.25rem 1.5rem;
    }
    .sidebar__nav {
      display: flex;
      flex-direction: column;
      gap: 0.25rem;
    }
    .nav-item {
      padding: 0.6rem 1.25rem;
      color: var(--text-muted);
      text-decoration: none;
      font-weight: 500;
      border-radius: 0 8px 8px 0;
      margin-right: 0.75rem;
      transition: color 0.15s, background 0.15s;
      &:hover { color: var(--text); background: var(--surface2); }
      &.active { background: var(--accent); color: #000; font-weight: 700; }
    }

    /* ── Mobile topbar ── */
    .topbar {
      display: none;
      align-items: center;
      justify-content: space-between;
      padding: 0 1rem;
      height: 52px;
      background: var(--surface);
      border-bottom: 1px solid var(--border);
      position: sticky;
      top: 0;
      z-index: 100;
    }
    .topbar__brand {
      font-size: 0.65rem;
      font-weight: 700;
      letter-spacing: 0.1em;
      color: var(--accent);
    }

    /* ── Hamburger button ── */
    .hamburger {
      display: flex;
      flex-direction: column;
      gap: 5px;
      background: none;
      border: none;
      cursor: pointer;
      padding: 6px;
      border-radius: 6px;
      transition: background 0.15s;
      &:hover { background: var(--surface2); }
    }
    .bar {
      display: block;
      width: 20px;
      height: 2px;
      background: var(--text);
      border-radius: 2px;
      transition: transform 0.25s ease, opacity 0.25s ease;
      transform-origin: center;
    }
    .bar:nth-child(1).open { transform: translateY(7px) rotate(45deg); }
    .bar:nth-child(2).open { opacity: 0; transform: scaleX(0); }
    .bar:nth-child(3).open { transform: translateY(-7px) rotate(-45deg); }

    /* ── Mobile drawer ── */
    .overlay {
      position: fixed;
      inset: 0;
      background: rgba(0,0,0,0.6);
      z-index: 200;
      animation: fadeIn 0.2s ease;
    }
    .drawer {
      position: fixed;
      top: 0;
      left: 0;
      height: 100vh;
      width: 240px;
      background: var(--surface);
      border-right: 1px solid var(--border);
      z-index: 300;
      display: flex;
      flex-direction: column;
      padding: 1.5rem 0;
      animation: slideIn 0.25s ease;
    }
    .drawer__brand {
      font-size: 0.65rem;
      font-weight: 700;
      letter-spacing: 0.1em;
      color: var(--accent);
      padding: 0 1.25rem 2rem;
    }
    .drawer-item {
      padding: 0.85rem 1.5rem;
      color: var(--text-muted);
      text-decoration: none;
      font-weight: 500;
      font-size: 1rem;
      transition: color 0.15s, background 0.15s;
      &:hover { color: var(--text); background: var(--surface2); }
      &.active { color: var(--accent); font-weight: 700; background: rgba(212,178,0,0.08); }
    }

    @keyframes fadeIn { from { opacity: 0; } to { opacity: 1; } }
    @keyframes slideIn { from { transform: translateX(-100%); } to { transform: translateX(0); } }

    /* ── Content ── */
    .content {
      flex: 1;
      overflow-y: auto;
      padding: 2rem;
    }

    /* ── Responsive ── */
    @media (max-width: 768px) {
      .sidebar { display: none; }
      .topbar { display: flex; }
      .shell { flex-direction: column; }
      .content { padding: 1rem; }
    }
  `],
})
export class ShellComponent {
  menuOpen = signal(false);

  toggleMenu() { this.menuOpen.update(v => !v); }
  closeMenu()  { this.menuOpen.set(false); }

  @HostListener('document:keydown.escape')
  onEsc() { this.closeMenu(); }
}
