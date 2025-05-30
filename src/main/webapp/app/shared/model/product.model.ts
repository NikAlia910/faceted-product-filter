import { ICategory } from 'app/shared/model/category.model';
import { IUser } from 'app/shared/model/user.model';

export interface IProduct {
  id?: number;
  name?: string;
  description?: string | null;
  price?: number;
  imageUrl?: string | null;
  rating?: number | null;
  category?: ICategory;
  user?: IUser | null;
}

export const defaultValue: Readonly<IProduct> = {};
