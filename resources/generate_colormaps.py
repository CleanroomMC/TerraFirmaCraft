import os
import shutil
from typing import Tuple

import gradients

SRC = '../src/main/resources/assets/tfc/textures/colormap/'


def main():
    make('sky.png', (0, 0, '#6697E7'), (255, 0, '#7ca5f7'), (0, 255, '#dec797'), (255, 255, '#ABAAE3'), (64, 64, '#6597CE'))
    make('fog.png', (0, 0, '#8FB1E9'), (255, 0, '#b4a1e7'), (0, 255, '#EDCC97'), (255, 255, '#d7d6f6'), (64, 64, '#b0d2f7'))

    make('water.png', (0, 0, '#4882C9'), (255, 0, '#273968'))

    make('grass.png', (0, 0, '#217C3E'), (255, 0, '#827759'), (0, 255, '#AFA83B'), (255, 255, '#A8833F'), (192, 0, '#729985'))

    make('foliage.png', (0, 0, '#1D6233'), (255, 0, '57776D'), (0, 255, '#8EA825'), (255, 255, '#9C8733'))
    make('foliage_fall.png', (0, 0, '#E8594C'), (255, 0, '#A85019'), (0, 255, '#E89740'), (255, 255, '#E0BA31'))
    make('foliage_winter.png', (0, 0, '#7C592B'))

    copy('water.png', 'water_fog.png')

    print('Done')


def make(image: str, *points: Tuple[int, int, str]):
    gradients.create(os.path.join(SRC, image), 256, 256, *points)


def copy(src: str, dest: str):
    shutil.copy(os.path.join(SRC, src), os.path.join(SRC, dest))


if __name__ == '__main__':
    main()
