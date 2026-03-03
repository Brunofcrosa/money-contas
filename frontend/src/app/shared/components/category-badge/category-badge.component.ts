import { Component, input } from '@angular/core';
import { CommonModule } from '@angular/common';
import {
    Category,
    CATEGORY_META,
} from '../../../core/models/transaction.model';

@Component({
    selector: 'app-category-badge',
    standalone: true,
    imports: [CommonModule],
    templateUrl: './category-badge.component.html',
})
export class CategoryBadgeComponent {
    readonly category = input.required<Category>();
    readonly size = input<'sm' | 'md'>('md');

    readonly meta = CATEGORY_META;
}

