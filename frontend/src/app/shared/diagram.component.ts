import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Diagram } from '../models';

@Component({
  selector: 'app-diagram',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './diagram.component.html'
})
export class DiagramComponent {
  @Input() d!: Diagram;
}
