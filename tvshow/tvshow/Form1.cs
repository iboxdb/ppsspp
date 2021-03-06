﻿using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.IO;
using System.Linq;
using System.Text;
using System.Windows.Forms;

namespace tvshow
{
    public partial class Form1 : Form
    {
        public Form1()
        {
            InitializeComponent();
        }

        private FileStream file;
        private Bitmap image;

        int s_width;
        int s_height;
        private void Form1_Load(object sender, EventArgs e)
        {
            timer1.Enabled = false; 
            OpenFileDialog openFileDialog1 = new OpenFileDialog();
     

            var path = @"C:\PSP\ppsspp\memstick\PSP\VIDEO\fd1492440010-38130.tvi";
            if (openFileDialog1.ShowDialog(this) == DialogResult.OK)
            {
                path = openFileDialog1.FileName;
            }


            file = new FileStream(path, FileMode.Open, FileAccess.Read, FileShare.None, 8 * 1024 * 1024, FileOptions.None);

            byte version = (byte)file.ReadByte();
            byte intsize = (byte)file.ReadByte();

            s_width = (byte)file.ReadByte();
            s_width |= (file.ReadByte() << 8);

            file.ReadByte();
            file.ReadByte();

            s_height = (byte)file.ReadByte();
            s_height |= (file.ReadByte() << 8);

            file.ReadByte();
            file.ReadByte();

            file.ReadByte();
            file.ReadByte();
            file.ReadByte();
            file.ReadByte();

            this.Width = s_width + 50;
            this.Height = s_height + 50;

            timer1.Enabled = true;
        }

        private void Form1_Paint(object sender, PaintEventArgs e)
        {
            if (image != null)
            {
                Graphics g = this.CreateGraphics();
                g.DrawImage(image, new Point(0, 0));
            }
        }

        private void next()
        {
            if (file.Position < file.Length)
            {
                int ms = (byte)file.ReadByte();
                ms |= (file.ReadByte() << 8);
                ms |= (file.ReadByte() << 16);
                ms |= (file.ReadByte() << 24);

                this.Text = ms.ToString();

                image = new Bitmap(s_width, s_height);
                for (int h = 0; h < s_height; h++)
                {
                    for (int w = 0; w < s_width; w++)
                    {
                        image.SetPixel(w, h, Color.FromArgb(file.ReadByte(), file.ReadByte(), file.ReadByte()));
                    }
                }
            }
            else
            {
                image = new Bitmap(s_width, s_height);
                for (int h = 0; h < s_height; h++)
                {
                    for (int w = 0; w < s_width; w++)
                    {
                        image.SetPixel(w, h, Color.FromArgb(100, 100, 100));
                    }
                }
                this.Text = "end";
            }
        }

        private void timer1_Tick(object sender, EventArgs e)
        {
            next();
            Refresh();
        }
    }
}
