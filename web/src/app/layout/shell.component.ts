import { Component } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';

@Component({
  selector: 'app-shell',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  template: `
    <div class="shell">
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
      &.active {
        background: var(--accent);
        color: #000;
        font-weight: 700;
      }
    }
    .content {
      flex: 1;
      overflow-y: auto;
      padding: 2rem;
    }
  `],
})
export class ShellComponent {}
